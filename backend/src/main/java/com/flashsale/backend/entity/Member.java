package com.flashsale.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "member")
public class Member extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "member_id", nullable = false, length = 36)
    private String memberId;

    @Column(name = "member_email", unique = true, nullable = false, length = 50)
    private String memberEmail;

    @Column(name = "member_pwd", nullable = false, length = 60)
    private String memberPwd;

    @Column(name = "member_name", nullable = false, length = 20)
    private String memberName;
}
