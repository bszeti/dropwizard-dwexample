# Dropwizard example

An example project using the [Dropwizard Java REST framework](http://www.dropwizard.io/) (v1.1.0) showing coding practices that were used successfully in real world projects:
- Project structure
- GET and POST resources
- Healthcheck with build info
- Customize Jackson ObjectMapper
- Custom exception mappers
- Add default (listing) servlet to admin
- AssetsBundle to serve static content
- Scheuled task (Dropwizard managed)
- Service manager for a service running on multiple threads (Dropwizard managed)
- Request response processed by multiple threads (using ListeningExecutorService)
- Dropwizard and Jersey client
- Async client call
- Tests using the client


## Related blog posts
- https://medium.com/@bszeti/dropwizard-project-structure-a9bc94867dbc
- https://medium.com/@bszeti/dropwizard-client-11728b65e47d
