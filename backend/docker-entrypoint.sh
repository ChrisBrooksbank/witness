#!/bin/sh
set -eu

# Docker creates a missing bind-mount source directory (such as ./data) owned
# by root, which the unprivileged witness user cannot write. Fix ownership as
# root, then drop privileges before starting the node.
if [ "$(id -u)" = "0" ]; then
	mkdir -p "$WITNESS_DATA_DIR"
	find "$WITNESS_DATA_DIR" ! -user witness -exec chown witness:witness {} +
	exec setpriv --reuid=witness --regid=witness --init-groups "$@"
fi

exec "$@"
