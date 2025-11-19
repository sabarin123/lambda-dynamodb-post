# Lambda DynamoDB POST API with SAM & CI/CD

[![CI/CD](https://github.com/sabarin123/lambda-dynamodb-post/actions/workflows/ci-cd.yaml/badge.svg)](https://github.com/<YOUR_GITHUB_USERNAME>/<REPO_NAME>/actions)
## Project Overview

This project demonstrates a **Java AWS Lambda function** integrated with **DynamoDB**, exposed via **API Gateway**, and deployed using **AWS SAM** with a **CI/CD pipeline via GitHub Actions and OIDC**.

- **Lambda Function**: Handles POST requests and writes data to DynamoDB.
- **DynamoDB Table**: Stores POST request data.
- **API Gateway**: Exposes Lambda via `/post` endpoint.
- **CI/CD**: Automatically builds, packages, and deploys on push to `main`.

---

## Project Structure

lambda-dynamodb-post/
│
├── src/main/java/org/example/lambda/handler/PostHandler.java
├── pom.xml
├── template.yaml
├── target/
│ └── lambda-dynamodb-post-1.0-SNAPSHOT.jar
└── .github/workflows/ci-cd.yaml


- `PostHandler.java` → Lambda handler  
- `template.yaml` → SAM template defining Lambda, DynamoDB, and API Gateway  
- `ci-cd.yaml` → GitHub Actions workflow for CI/CD  

---

## Prerequisites

- AWS account with OIDC role for GitHub Actions  
- AWS SAM CLI installed  
- Maven installed (Java 17)  
- GitHub repository  

---

## Setup Instructions

### 1. Create S3 Bucket (for SAM artifacts)

```bash
aws s3 mb s3://sam-bucket --region eu-north-1
2. Configure GitHub Secrets
| Secret Name       | Value                                      |
| ----------------- | ------------------------------------------ |
| `AWS_DEPLOY_ROLE` | ARN of OIDC role for GitHub Actions        |
| `S3_BUCKET`       | Name of SAM artifact bucket (`sam-bucket`) |


3. Build and Deploy Locally (Optional)
mvn clean package
sam package --template-file template.yaml --s3-bucket sam-bucket --output-template-file packaged.yaml
sam deploy --template-file packaged.yaml --stack-name lambda-dynamodb-post --capabilities CAPABILITY_IAM


4. CI/CD Deployment via GitHub Actions

On push to main, workflow performs:

Checkout repo

Configure AWS credentials via OIDC

Build Maven project

SAM package → uploads artifact to S3

SAM deploy → creates/updates Lambda, DynamoDB, API Gateway

5. Test API

POST Request Example (Postman or curl)

URL: https://<API_ID>.execute-api.eu-north-1.amazonaws.com/Prod/post
Headers:
  Content-Type: application/json
Body:
{
  "name": "Hariharan",
  "email": "sabari@example.com",
  "message": "Testing Lambda API"
}

Verify data appears in DynamoDB table MyDynamoDBTable

6. Notes / Troubleshooting

Ensure handler class matches Handler in template.yaml:

Handler: org.example.lambda.handler.PostHandler::handleRequest

Make sure JAR filename in CodeUri matches Maven output

GitHub Actions must have OIDC role permissions for Lambda, CloudFormation, S3, DynamoDB, API Gateway

Lambda logs are in CloudWatch → useful for debugging

References

AWS SAM Documentation

AWS Lambda Java Handler

GitHub OIDC Authentication with AWS
