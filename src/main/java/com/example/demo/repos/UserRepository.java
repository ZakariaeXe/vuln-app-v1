package com.example.demo.repos;
import com.example.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

    public interface UserRepository extends JpaRepository<User, Long> {


    }

