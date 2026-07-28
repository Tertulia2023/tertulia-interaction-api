\# TERTÚLIA Interaction API



External HTTP/JSON adapter and local executable application for the SIT TERTÚLIA Interaction Layer.



\## Architecture



```text

external client

&#x20;     ↓

HTTP / JSON

&#x20;     ↓

POST /interactions

&#x20;     ↓

Interaction API

&#x20;     ↓

Interaction Layer

&#x20;     ↓

Query Service

&#x20;     ↓

TERTÚLIA Kernel

