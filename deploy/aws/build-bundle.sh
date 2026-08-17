#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
WORKLOAD_DIR="${WORKLOAD_DIR:-${ROOT_DIR}/../trainer-workload-service}"
BUILD_DIR="${ROOT_DIR}/build/aws"
RUNTIME_DIR="${BUILD_DIR}/runtime"
CLOUDSHELL_DIR="${BUILD_DIR}/cloudshell"

if [[ ! -x "${WORKLOAD_DIR}/gradlew" ]]; then
  echo "Trainer workload service was not found at ${WORKLOAD_DIR}" >&2
  exit 1
fi

"${ROOT_DIR}/gradlew" clean test bootJar
"${ROOT_DIR}/gradlew" -p "${ROOT_DIR}/discovery-server" clean test bootJar
"${WORKLOAD_DIR}/gradlew" -p "${WORKLOAD_DIR}" clean check bootJar

rm -rf "${RUNTIME_DIR}" "${CLOUDSHELL_DIR}"
mkdir -p "${RUNTIME_DIR}/artifacts" "${RUNTIME_DIR}/nginx" "${CLOUDSHELL_DIR}"

find_jar() {
  find "$1" -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -print -quit
}

cp "$(find_jar "${ROOT_DIR}/build/libs")" "${RUNTIME_DIR}/artifacts/gym-management.jar"
cp "$(find_jar "${ROOT_DIR}/discovery-server/build/libs")" "${RUNTIME_DIR}/artifacts/discovery-server.jar"
cp "$(find_jar "${WORKLOAD_DIR}/build/libs")" "${RUNTIME_DIR}/artifacts/trainer-workload-service.jar"
cp "${ROOT_DIR}/deploy/aws/runtime.Dockerfile" "${RUNTIME_DIR}/runtime.Dockerfile"
cp "${ROOT_DIR}/deploy/aws/docker-compose.yml" "${RUNTIME_DIR}/docker-compose.yml"
cp "${ROOT_DIR}/deploy/aws/nginx/default.conf" "${RUNTIME_DIR}/nginx/default.conf"
ruby "${ROOT_DIR}/docs/swagger/merge-openapi.rb"
cp -R "${ROOT_DIR}/docs/swagger" "${RUNTIME_DIR}/docs"

(cd "${RUNTIME_DIR}" && zip -qr "${CLOUDSHELL_DIR}/gym-runtime.zip" .)
cp "${ROOT_DIR}/deploy/aws/cloudformation.yml" "${CLOUDSHELL_DIR}/cloudformation.yml"
cp "${ROOT_DIR}/deploy/aws/deploy.sh" "${CLOUDSHELL_DIR}/deploy.sh"
(cd "${CLOUDSHELL_DIR}" && zip -qr "${BUILD_DIR}/gym-aws-cloudshell.zip" .)

echo "AWS deployment package: ${BUILD_DIR}/gym-aws-cloudshell.zip"
