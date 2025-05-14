FROM nginx:alpine
COPY wedding-website/ /usr/share/nginx/html
EXPOSE 80
