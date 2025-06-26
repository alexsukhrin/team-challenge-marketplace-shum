#!/bin/bash
echo "📦 Loading .env variables..."
if [ -f .env ]; then
  set -a
  source .env
  set +a
  echo "✅ .env loaded"
else
  echo "⚠️  .env file not found, skipping"
fi

clojure -X:migrate