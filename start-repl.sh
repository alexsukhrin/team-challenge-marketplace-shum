#!/bin/bash

echo "📦 Loading .env variables..."
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
  echo "✅ .env loaded"
else
  echo "⚠️  .env file not found, skipping"
fi

echo "🟢 Starting REPL in team-challenge.marketplace-shum namespace..."
clj -M:dev
