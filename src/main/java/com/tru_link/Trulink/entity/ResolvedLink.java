package com.tru_link.Trulink.entity;

import java.util.UUID;

public record ResolvedLink(UUID linkId, String originalUrl, boolean hasOwner) {
}
