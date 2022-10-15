package org.cmccx.src.user;

import org.cmccx.utils.JwtService;
import org.cmccx.utils.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserProvider {

    private final UserDao userDao;
    private final JwtService jwtService;
    private final S3Service s3Service;
    
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UserProvider(UserDao userDao, JwtService jwtService, S3Service s3Service) {
        this.userDao = userDao;
        this.jwtService = jwtService;
        this.s3Service = s3Service;
    }


}
