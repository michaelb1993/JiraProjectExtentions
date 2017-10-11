package com.michabond.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Unique;

public interface User extends Entity {

    @NotNull
    @Unique
    String getName();
    void setName(String week);

    @OneToMany
    Subscription[] getSubscriptions();
}
