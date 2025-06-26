FROM clojure:openjdk-17-tools-deps
WORKDIR /app

# Спочатку копіюємо залежності для кешування
COPY deps.edn .
RUN clj -P

# Копіюємо решту файлів
COPY . .

# Збираємо uberjar і створюємо entrypoint-скрипт
RUN clj -X:uberjar && \
    printf '#!/bin/sh\n\
if [ "$1" = "migrate" ]; then\n\
  echo "Running migrations..."\n\
  exec clojure -X:migrate\n\
elif [ "$1" = "app" ]; then\n\
  echo "Starting application..."\n\
  exec java -jar target/app.jar\n\
else\n\
  exec "$@"\n\
fi' > /usr/local/bin/entrypoint.sh && \
    chmod +x /usr/local/bin/entrypoint.sh

ENTRYPOINT ["entrypoint.sh"]

# За замовчуванням буде запускатися додаток
CMD ["app"] 