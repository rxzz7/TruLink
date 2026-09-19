package com.tru_link.Trulink.entity;

import java.util.UUID;

//Because I only need 3 fields, I use an interface projection.
// Spring Data JPA provides an implementation of that interface for me, so I don't have to create a separate class.
public interface ResolvedLinkProjection {
    UUID getLinkId();

    String getOriginalUrl();

    UUID getUserId();

}
