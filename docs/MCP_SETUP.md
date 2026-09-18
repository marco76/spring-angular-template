# MCP Setup

This template separates two different MCP use cases:

- **Developer MCP servers** help your coding agent or IDE read current framework documentation and run framework tools.
- **Runtime Spring AI MCP** lets your Spring Boot application expose or consume MCP tools at runtime.

Start with developer MCP servers. Add runtime MCP only when the application itself needs AI tool integration.

## Recommended Developer MCP Servers

Add these to the MCP configuration for your coding agent or IDE.

Different clients use different file names and top-level keys. Many clients accept `mcpServers`; VS Code-style configs often use `servers`. Keep the server blocks the same and adjust the wrapper to your client.

## Angular CLI MCP

Angular CLI includes an experimental MCP server. It can provide Angular best practices, examples, and selected CLI-powered tools.

Project or global MCP config:

```json
{
  "mcpServers": {
    "angular-cli": {
      "command": "npx",
      "args": ["-y", "@angular/cli", "mcp"]
    }
  }
}
```

Read-only mode is safer for documentation lookup:

```json
{
  "mcpServers": {
    "angular-cli": {
      "command": "npx",
      "args": ["-y", "@angular/cli", "mcp", "--read-only"]
    }
  }
}
```

You can also run this from the frontend directory to print client-specific setup hints:

```bash
cd frontend
npx -y @angular/cli mcp
```

Source: https://v21.angular.dev/ai/mcp

## MDN MCP

MDN provides a remote MCP server for web platform documentation, CSS, HTML, JavaScript, and browser compatibility data.

Use an HTTP/remote MCP configuration:

```json
{
  "mcpServers": {
    "mdn": {
      "transport": "http",
      "url": "https://mcp.mdn.mozilla.net/"
    }
  }
}
```

Some clients configure remote servers through a command instead of JSON. For those clients, add an HTTP MCP server named `mdn` with:

```text
https://mcp.mdn.mozilla.net/
```

MDN describes the server as experimental and notes that queries may be logged during the experiment. Avoid sending private code or secrets in documentation prompts.

Source: https://developer.mozilla.org/en-US/mcp

## Spring Documentation MCP

Spring AI provides official runtime MCP support, but there is not a single official Spring documentation MCP server in the same form as Angular CLI or MDN.

For developer documentation lookup, use a community Spring documentation MCP server and pin or review it before adopting it in a company template:

```json
{
  "mcpServers": {
    "spring-docs": {
      "command": "npx",
      "args": ["-y", "@enokdev/springdocs-mcp@latest"]
    }
  }
}
```

For stricter reproducibility, replace `@latest` with a reviewed version:

```json
{
  "mcpServers": {
    "spring-docs": {
      "command": "npx",
      "args": ["-y", "@enokdev/springdocs-mcp@1.2.8"]
    }
  }
}
```

Review the package, permissions, and network behavior before enabling it in automated environments.

Package: https://www.npmjs.com/package/@enokdev/springdocs-mcp

## Combined Developer MCP Example

Use this as the starting point for clients that support `mcpServers`:

```json
{
  "mcpServers": {
    "angular-cli": {
      "command": "npx",
      "args": ["-y", "@angular/cli", "mcp", "--read-only"]
    },
    "mdn": {
      "transport": "http",
      "url": "https://mcp.mdn.mozilla.net/"
    },
    "spring-docs": {
      "command": "npx",
      "args": ["-y", "@enokdev/springdocs-mcp@1.2.8"]
    }
  }
}
```

## Runtime Spring AI MCP Server

Use this only if your Spring Boot app should expose tools, resources, or prompts to MCP clients.

Add the dependency in `backend/pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

Enable it in an explicit profile, for example `application-mcp.yml`:

```yaml
spring:
  ai:
    mcp:
      server:
        name: example-app-mcp
        protocol: STREAMABLE
        type: SYNC
```

Create a tool:

```java
package com.example.app.tools;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Service;

@Service
public class AppInfoTool {

    @McpTool(description = "Return basic application status")
    public String status() {
        return "Application is running";
    }
}
```

Security rule: HTTP MCP transports expose an endpoint that can list and invoke registered tools unless you protect it. Do not expose `/mcp` beyond localhost without Spring Security, authentication, and authorization.

Sources:

- https://docs.spring.io/spring-ai/reference/guides/getting-started-mcp.html
- https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html

## Runtime Spring AI MCP Client

Use this only if your Spring Boot app should call external MCP servers.

Add the dependency:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
```

Configure a remote HTTP MCP server:

```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        name: example-app-mcp-client
        type: SYNC
        streamable-http:
          connections:
            local-tools:
              url: http://localhost:8081
              endpoint: /mcp
```

Or configure a stdio server:

```yaml
spring:
  ai:
    mcp:
      client:
        stdio:
          connections:
            local-filesystem:
              command: npx
              args:
                - -y
                - "@modelcontextprotocol/server-filesystem"
                - .
```

Source: https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html

## Team Defaults

Recommended defaults for this template:

- Enable Angular CLI MCP in read-only mode.
- Enable MDN MCP for web platform lookup.
- Use the Spring documentation MCP only after reviewing the community package.
- Keep runtime Spring AI MCP disabled until the app has a concrete MCP use case.
- Never commit MCP secrets or personal tokens.
