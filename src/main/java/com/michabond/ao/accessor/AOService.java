package com.michabond.ao.accessor;

import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface AOService {

    void collectGarbage();
}
