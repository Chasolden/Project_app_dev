FROM nginx:1.28.0-alpine3.21-slim
COPY wedding-website/ /usr/share/nginx/html
EXPOSE 80
