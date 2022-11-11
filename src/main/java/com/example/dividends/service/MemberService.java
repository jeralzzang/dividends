package com.example.dividends.service;

import com.example.dividends.exception.impl.AlreadyExistsUserException;
import com.example.dividends.model.Auth;
import com.example.dividends.model.MemberEntity;
import com.example.dividends.persist.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MemberService implements UserDetailsService {

  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return this.memberRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("찾을수 없는 유저정보 -> "+ username));
  }

  public MemberEntity register(Auth.SignUp member){
    boolean exists = this.memberRepository.existsByUsername(member.getUsername());
    if(exists){
      throw new AlreadyExistsUserException();
    }

    member.setPassword(this.passwordEncoder.encode(member.getPassword()));
    var result = this.memberRepository.save(member.toEntity());

    return result;
  }

  public MemberEntity authnticate(Auth.SignIn member){
    var user = this.memberRepository.findByUsername(member.getUsername())
        .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디 입니다."));

    if(!this.passwordEncoder.matches(member.getPassword(), user.getPassword())){
      throw new RuntimeException("비밀번호가 일치하지 않습니다.");
    }
    return user;
  }
}
