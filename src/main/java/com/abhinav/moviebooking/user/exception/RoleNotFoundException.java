package com.abhinav.moviebooking.user.exception;

import com.abhinav.moviebooking.common.exception.BusinessException;
import com.abhinav.moviebooking.util.ErrorCode;

public class RoleNotFoundException extends BusinessException {

    public RoleNotFoundException(String roleName) {
        super(ErrorCode.ROLE_NOT_FOUND,
                "Role not found : " + roleName);
    }
}
