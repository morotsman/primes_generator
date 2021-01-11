# The prime generator

> Develop a set of 2 small services that work together to deliver a sequence of prime numbers up to a given number. 

Due to me being a bit lazy I put both services in the same repository, hope that is ok?

## Run the app

    sbt run

Then select which service to start.

Manually test the app:

Install [httpie] and then:

    http --stream GET :8081/prime/17

I'm building the app with [sbt-native-packager].

Alternative way to start the app:

    sbt stage
    cd target/universal/stage
    ./bin/<app-name>

## PrimeNumberService

> The `prime-number-server` does the actual Prime number calculation - it serves responses continuously over Finagle OR gRPC and uses proper abstractions to communicate failure 

### Protocol

I have never used Thrift or gRCP before so I had to do some reading up on the subject. 

When investigating Thrift and streaming, I found [articles] like this one that discourage me to use it.  

On the other hand, when I read about gRCP I found that it has support for streaming. I also found that Akka has [support] for it and since I'm fond of the library I decided to go for gRCP and Akka.

### Prime number generator

I'm not a prime number generator expert, so I used [google] to find a good algorithm:
    
    val primes: Stream[Int] = 2 #:: Stream.from(3, 2).filter(i => primes.takeWhile(j => j * j <= i).forall(k => i % k > 0))

### proto

Ok, now that the most important thing is in place lets investigate gRPC.

You can define your interface in a proto file:

    service PrimesService {
      rpc generatePrimes (PrimesRequest) returns (stream PrimesReply) {}
    }
    
    message PrimesRequest {
      int32 upTo = 1;
    }
    
    message PrimesReply {
      int64 prime = 1;
    }

The interface allows the client to call the generatePrime "function". The user provides a `PrimesRequest` that contains the `up to` number. 

The clients gets back a stream of PrimesReply, each containing a valid prime number. 

### Generate Stream of primes

To get the stream we are supposed to return to the client I wrap the primes stream (defined above) in a Source.

    override def generatePrimes(in: PrimesRequest): Source[PrimesReply, NotUsed] = {
        if(in.upTo >= 0) {
            Source(primes).takeWhile(p => p <= in.upTo).map(p => PrimesReply(p))
        } else {
            val error = new GrpcServiceException(Status.INVALID_ARGUMENT.withDescription("upTo must be greater or equal to zero"))
            Source.failed(error)
        }
    }

The client is notified that upTo must be greater or equal to zero if upTo is below zero.

### Tests

I found it quite simple to write unit tests and functional tests for the service, but here I refer to the code base.

### Start the service

    def start(location: String, port: Int)(implicit system: ActorSystem, ec: ExecutionContext): Future[Http.ServerBinding] = {
        val service = PrimesServiceHandler(new PrimeNumberServiceImpl())

        val binding = Http().newServerAt(location, port)
        .bind(service)
        .map(_.addToCoordinatedShutdown(hardTerminationDeadline = shutdownTimeout.millis))

        binding.onComplete{
            case Success(binding) =>
                logger.info(s"Prime genrator server bound to: ${binding.localAddress}")
            case Failure(exception) =>
                logger.info(s"Could not start the prime generator service: ${exception.getMessage}")
        }

        binding
  }

      
Here we are using the code generated from the proto file (`PrimesServiceHandler`) to start the service.

## Proxy

So far so good, now one of the services is in place, lets continue with the proxy service.


### Requirements

> The `proxy-service` acts as an entry point to the outside world. 
>  * It's main tasks are: 
>    * expose a HTTP endpoint over REST responding to GET /prime/<number> that continuously streams all prime numbers up to a given number e.g. /prime/17 should return 2,3,5,7,11,13,17. 
>    * delegates the actual calculation to the second microservice via a Finagle-Thrift OR gRPC RPC call 
>    * handles wrong inputs in a proper way 

### Rest API

I decided to use akka streams to expose the REST api:

    private def primes(numberOfPrimes: Int): Source[PrimesReply, NotUsed] = client.generatePrimes(PrimesRequest(numberOfPrimes))
    
    val route: Route =
        get {
        path("prime" / IntNumber) { numberOfPrimes => {
            logRequest(s"prime/$numberOfPrimes", Logging.InfoLevel) {
                complete {
                    val source = primes(numberOfPrimes).map(n => ByteString(s"${n.prime}\n"))
                    // It's possible to add some error handling here: https://doc.akka.io/docs/akka/current/stream/stream-error.html
                    HttpEntity(ContentTypes.`text/plain(UTF-8)`, source)
                }
            }
        }
      }
    }

I define a source `primes` that is connected to the prime service, I then return the source in the response. 

Akka http is handling the error cases (Not Found) etc. One caveat is that IntNumber only matches positive integers. This means that if -17 is sent in the request is rejected with NOT_FOUND instead of BAD_REQUEST which would be more appropriate. 


### Proto file

We reuse the proto file to generate the code that is necessary to communicate with the prime number generator.


## Graceful shutdown

Find the process:

    ps -e | grep java | grep ProxyServer
    kill -SIGTERM 31573

When I tested this I didn't get the behaviour I expected when using [addToCoordinatedShutdown], so I have to investigate this more.

        
## Manual test

To manually test this one can use [httpie]. Here are is an example:

    http --stream GET :8081/prime/17
    HTTP/1.1 200 OK
    Content-Type: text/plain; charset=UTF-8
    Date: Fri, 08 Jan 2021 15:39:42 GMT
    Server: akka-http/10.2.2
    Transfer-Encoding: chunked
    
    2
    
    3
    
    5
    
    7
    
    11
    
    13
    
    17   
    
    
## Back pressure     

I did a small experiment with back pressure:

    curl --limit-rate 10000b 127.0.0.1:8081/prime/2147483647
    
It seems to work but not exactly as I expected, but I read the following [post] that I think explains the behaviour which is.

    * The prime generator gets a request
    
    1 It starts to generate a lot primes but then stops
    2 The client receives primes and eventually start to catch up with the server
    3 The prime generator generates a new batch of primes.
    4 Repeat from 1


###The assignment completed?

So now the functionality is in place with basic error handling. 

However there are still things that needs to be done before the services are ready for production. I however think that they are out of scope for this assignment? I list some of them below.

## Linting/Check style

Add some sort of linting tool to see that we are following the code standards

## SonarQube

Add static analytic of the code, code coverage etc.

## Contract testing

When working with GraphQL which also has a defined schema I came in contact with [contract testing]. The contract tests great for finding breaking changes in API's.

It would be possible to have something similar for gRCP, but to investigate that is out of scope for this assignment I think?

## Error Handling

The requirements did not mention what should happen if "bad" things happens when talking over gRCP, for example if the prime number generator goes down while we stream.

You can do [retries and other things] but I assumed that this is out of scope?

## Encryption/Security

Tls is not enabled. No authorization/authentication.

## Metrics

No service is complete without adding metrics to it, but I assume that this is out of scope as well?

## Deployment pipeline

Create a Jenkins file (or similar) so that we can build/test and deploy things. Out of scope?

## Docker

Produce docker images so that the services can be deployed on Kubernetes. Out of scope?

## Deployment script

A Ansible playbook or something similar.

And so on.

[articles]: https://grokbase.com/t/thrift/user/153r7sfyyb/streaming-support-for-thrift-in-java
[support]: https://doc.akka.io/docs/akka-grpc/current/server/walkthrough.html
[google]: https://stackoverflow.com/questions/20985539/scala-erastothenes-is-there-a-straightforward-way-to-replace-a-stream-with-an/20991776#comment31606932_20986428
[httpie]: https://httpie.io/
[contract testing]: https://pactflow.io/blog/what-is-contract-testing/#:~:text=Contract%20testing%20is%20a%20methodology,both%20parties%20adhere%20to%20it.
[retries and other things]: https://doc.akka.io/docs/akka/current/stream/stream-error.html
[sbt-native-packager]: https://sbt-native-packager.readthedocs.io/en/latest/archetypes/java_app/index.html
[post]: https://discuss.lightbend.com/t/backpressure-support-with-akka-grpc/3192/4
[addToCoordinatedShutdown]: https://doc.akka.io/docs/akka-http/current/server-side/graceful-termination.html
