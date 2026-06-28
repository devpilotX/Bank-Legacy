# Build the public site, then serve the static files with nginx. The public site is
# all static, so there is nothing to proxy.

FROM node:24-alpine AS build
WORKDIR /app
# Copy .npmrc too, so npm ci uses the same settings (legacy-peer-deps) as local installs.
COPY package.json package-lock.json .npmrc ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
