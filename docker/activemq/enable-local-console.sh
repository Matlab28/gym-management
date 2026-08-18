#!/usr/bin/env bash
set -euo pipefail

jetty_config="/opt/apache-activemq/conf/jetty.xml"

# Docker forwards host requests through a private bridge, while ActiveMQ 6.2
# permits only in-container loopback unless the bundled RFC 1918 rules are enabled.
if grep -q "Example: allow standard private network ranges" "${jetty_config}"; then
    sed -i '262,265d;290d' "${jetty_config}"
fi

exec /usr/local/bin/entrypoint.sh "$@"
