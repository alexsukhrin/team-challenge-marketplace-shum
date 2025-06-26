FROM clojure:openjdk-17-tools-deps
WORKDIR /app

# Копіюємо всі файли проекту
COPY . .

# (Опційно) Встановити Clojure, якщо потрібно (для Alpine)
# RUN apk add --no-cache bash curl && \
#     curl -O https://download.clojure.org/install/linux-install-1.11.1.1273.sh && \
#     chmod +x linux-install-1.11.1.1273.sh && \
#     ./linux-install-1.11.1.1273.sh && \
#     rm linux-install-1.11.1.1273.sh

EXPOSE 7888

ENTRYPOINT ["clojure", "-M", "-m", "nrepl.cmdline", "--port", "7888"]
