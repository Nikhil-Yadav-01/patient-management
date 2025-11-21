#!/bin/bash
set -e # Stops the script if any command fails

awslocal --endpoint-url=http://localhost:4566 cloudformation delete-stack \
    --stack-name patient-management

awslocal --endpoint-url=http://localhost:4566 cloudformation deploy \
    --stack-name patient-management \
    --template-file "./cdk.out/localstack.template.json"

awslocal --endpoint-url=http://localhost:4566 elbv2 describe-load-balancers \
    --query "LoadBalancers[0].DNSName" --output text
