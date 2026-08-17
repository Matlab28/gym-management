#!/usr/bin/env bash
set -euo pipefail

REGION="${AWS_REGION:-eu-west-2}"
STACK_NAME="${STACK_NAME:-gym-microservices}"
ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
BUCKET="gym-microservices-${ACCOUNT_ID}-${REGION}"
ARTIFACT_KEY="releases/gym-runtime.zip"

if ! aws s3api head-bucket --bucket "${BUCKET}" 2>/dev/null; then
  aws s3api create-bucket \
    --bucket "${BUCKET}" \
    --region "${REGION}" \
    --create-bucket-configuration "LocationConstraint=${REGION}"
fi

aws s3 cp gym-runtime.zip "s3://${BUCKET}/${ARTIFACT_KEY}" --region "${REGION}"
aws cloudformation deploy \
  --region "${REGION}" \
  --stack-name "${STACK_NAME}" \
  --template-file cloudformation.yml \
  --capabilities CAPABILITY_IAM \
  --parameter-overrides \
    "ArtifactBucket=${BUCKET}" \
    "ArtifactKey=${ARTIFACT_KEY}" \
    "InstanceType=t3.small"

aws cloudformation describe-stacks \
  --region "${REGION}" \
  --stack-name "${STACK_NAME}" \
  --query 'Stacks[0].Outputs' \
  --output table
