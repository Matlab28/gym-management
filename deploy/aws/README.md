# AWS deployment

This package runs PostgreSQL, ActiveMQ Classic, Eureka, Gym Management,
Trainer Workload, and Nginx on one `t3.small` EC2 instance in `eu-west-2`.
Only HTTP port 80 is public. PostgreSQL, ActiveMQ ports `61616`/`8161`, and all
Spring service ports stay on the private Docker network. EC2 access is
available through AWS Systems Manager Session Manager; SSH is not exposed.

## Build

```bash
./deploy/aws/build-bundle.sh
```

The command runs every test suite, enforces the Trainer Workload 80% line
coverage gate, and creates:

```text
build/aws/gym-aws-cloudshell.zip
```

## Deploy from AWS CloudShell

Upload and extract `gym-aws-cloudshell.zip`, then run:

```bash
chmod +x deploy.sh
./deploy.sh
```

CloudFormation prints the API and unified static Swagger URLs. `/docs/` serves
`index.html` plus both OpenAPI YAML files. The service selector switches
between Gym Management API and Trainer Workload API without leaving the page,
and the API Base URL field changes the target for both specifications.

Workload updates use persistent JSON messages on `trainer.workload.events`.
The Trainer Workload container runs `2-6` competing consumers, retries failed
messages with exponential backoff, and routes exhausted messages to
`ActiveMQ.DLQ`. The deployment generates the broker password; it is not stored
in source control.

## Remove

```bash
aws cloudformation delete-stack --region eu-west-2 --stack-name gym-microservices
aws cloudformation wait stack-delete-complete --region eu-west-2 --stack-name gym-microservices
aws s3 rm s3://gym-microservices-$(aws sts get-caller-identity --query Account --output text)-eu-west-2 --recursive
aws s3 rb s3://gym-microservices-$(aws sts get-caller-identity --query Account --output text)-eu-west-2
```
