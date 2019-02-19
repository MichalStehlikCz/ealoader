package com.provys.object;

import javax.annotation.Nonnull;

public interface ProvysNmObject extends ProvysObject {
    /**
     * @return internal name (attribute NAME_NM)
     */
    @Nonnull
    String getNameNm();
}
