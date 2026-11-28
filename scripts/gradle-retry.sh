#!/usr/bin/env bash
# Roda o Gradle e repete a execução só quando o Maven Central limita as requisições (HTTP 429) ou fica
# indisponível (502, 503, 504). Isso acontece de vez em quando com as máquinas compartilhadas do GitHub
# Actions e não tem relação com o código. Qualquer outro erro (teste quebrado, erro de lint) falha na hora.
#
# Uso: bash .github/scripts/gradle-retry.sh <tarefas do Gradle>
set -u

attempts=3
wait_seconds="${GRADLE_RETRY_WAIT:-60}"
log="${RUNNER_TEMP:-/tmp}/gradle-retry.log"

for ((attempt = 1; attempt <= attempts; attempt++)); do
  ./gradlew "$@" --no-daemon 2>&1 | tee "$log"
  status=${PIPESTATUS[0]}

  if [ "$status" -eq 0 ]; then
    exit 0
  fi
  if ! grep -Eq "status code (429|502|503|504)" "$log"; then
    exit "$status"
  fi
  if [ "$attempt" -lt "$attempts" ]; then
    echo "::warning::O repositório Maven limitou as requisições. Tentativa $attempt de $attempts; repetindo em ${wait_seconds}s."
    sleep "$wait_seconds"
  fi
done

echo "::error::O repositório Maven continuou recusando as requisições depois de $attempts tentativas."
exit 1
