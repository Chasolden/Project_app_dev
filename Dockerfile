FROM nginx:alpine@sha256:<latest-fixed-sha>
COPY wedding-website/ /usr/share/nginx/html
EXPOSE 80
