FROM nginx:1.27.0-alpine
COPY wedding-website/ /usr/share/nginx/html
EXPOSE 80
