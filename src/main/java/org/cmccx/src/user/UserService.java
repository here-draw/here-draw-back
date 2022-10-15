package org.cmccx.src.user;

import org.cmccx.utils.JwtService;
import org.cmccx.utils.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserDao userDao;
    private final UserProvider userProvider;
    private final JwtService jwtService;
    private final S3Service s3Service;

    @Autowired
    public UserService(UserDao userDao, UserProvider userProvider, JwtService jwtService, S3Service s3Service) {
        this.userDao = userDao;
        this.userProvider = userProvider;
        this.jwtService = jwtService;
        this.s3Service = s3Service;
    }
    
}
