

```markdown
# AI Image Processor

This project is a Spring Boot application that provides APIs for communicating with AILLMModel 

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Java 8 or higher
- MongoDB
- Gradle

### Installing

1. Clone the repository: `git clone https://github.com/dsouzac-avaya/AIImageProcessor.git`
2. Navigate to the project directory: `cd AIImageProcessor`
3. Build the project: `gradle build`
4. Run the application: `gradle bootRun`

## Usage

The application provides the following endpoints:

- `POST /uploadAsync`: Upload an image to MongoDB asynchronously.
- `GET /image/{filename}`: Retrieve an image from MongoDB.
- `DELETE /image/{filename}`: Delete an image from MongoDB.

## Built With

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MongoDB](https://www.mongodb.com/)

## Authors
https://github.com/cedric10101980/Cedric-DSouza

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
```

You can customize this template according to your project's needs.

## Import Certificate in JVM Truststore
cd $JAVA_HOME/lib/security/
keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias zscaler -file <file location>/zscaler.cer

## Install on AKS

```sh
# Create namespace
kubectl create namespace outbound-pom

# Create Docker registry secret

# Create generic secret for application
kubectl create secret generic app-secrets \
  --from-literal=SPRING_DATA_MONGODB_URI='mongodb+srv://mongo:oj******iimageapp.lvklnwl.mongodb.net/mongodb_container?retryWrites=true&w=majority&appName=AIImageAPP' \
  --from-literal=OPENAI_API_KEY='sk-proj-SF3RR****WRNGGV9uUh' \
  --from-literal=SECURITY_SECRET_KEY=aHR0cHM6*****Etb2JhYXMuY29tLw== \
  -n outbound-pom

# Install Helm chart
helm install outbound-aiinfoservice ./build_scripts --namespace outbound-pom
```

## Upgrade on AKS
```sh
helm upgrade outbound-aillmprocessor  ./build_scripts --namespace outbound-pom --set image.tag=v.0.0.5
```

## Letsencrypt Certificate
Follow these steps at https://medium.com/@tayeblagha/create-free-ssl-certificate-and-configure-it-for-spring-boot-web-applications-a2106d97b733
Change the application to run in http mode
```sh
sudo certbot certonly --manual --preferred-challenges=http -d hackathon.avaya-obaas.com
```
