(() => {
    const fallbackOrigin = "http://localhost:8080";
    const origin = window.location.origin === "null" ? fallbackOrigin : window.location.origin;

    const renderSwagger = (specification) => {
        window.ui = SwaggerUIBundle({
            dom_id: "#swagger-ui",
            spec: specification,
            deepLinking: true,
            displayRequestDuration: false,
            docExpansion: "list",
            filter: false,
            layout: "BaseLayout",
            operationsSorter: "alpha",
            persistAuthorization: true,
            presets: [SwaggerUIBundle.presets.apis],
            tagsSorter: "alpha",
            tryItOutEnabled: false
        });
    };

    fetch("gym-platform.json")
        .then((response) => {
            if (!response.ok) {
                throw new Error(`Unable to load OpenAPI document (${response.status})`);
            }
            return response.json();
        })
        .then((specification) => {
            specification.servers = [
                {
                    url: origin,
                    description: "Gym Management microservice"
                },
                {
                    url: `${origin}/workload`,
                    description: "Trainer Workload microservice"
                }
            ];
            renderSwagger(specification);
        })
        .catch((error) => {
            document.getElementById("swagger-ui").textContent = error.message;
        });
})();
