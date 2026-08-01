FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
ARG VITE_ENABLE_DEV_TOOLS=false
ARG VITE_APP_VERSION="Local Development"
ARG VITE_DEPLOY_ENV="Local environment"
ENV VITE_ENABLE_DEV_TOOLS=$VITE_ENABLE_DEV_TOOLS
ENV VITE_APP_VERSION=$VITE_APP_VERSION
ENV VITE_DEPLOY_ENV=$VITE_DEPLOY_ENV
RUN npm run build

FROM nginx:1.27-alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

ENV BACKEND_UPSTREAM=backend:8080
ENV DOLLAR=$

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
