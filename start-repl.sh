#!/bin/bash

echo "📦 Loading .env variables..."
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
  echo "✅ .env loaded"
else
  echo "⚠️  .env file not found, skipping"
fi

echo "🟢 Starting nREPL on port 7888..."
clj -M:repl
