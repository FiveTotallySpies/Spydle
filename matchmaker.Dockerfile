FROM gradle:jdk17 as build
WORKDIR /app
COPY --chown=gradle:gradle . /app
RUN gradle :matchmaker:build -x test --no-daemon

# Start a new stage to create a smaller image
FROM amazoncorretto:17 as run
WORKDIR /app
COPY --from=build /app/matchmaker/build/libs/*.jar /app/
COPY --from=build /app/matchmaker/entry.sh /app/

ENTRYPOINT ["sh", "entry.sh"]