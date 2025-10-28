package com.found404.marketbee.user;

import com.found404.marketbee.auth.jwt.JwtProvider;
import com.found404.marketbee.store.Store;
import com.found404.marketbee.store.StoreRepository;
import com.found404.marketbee.user.dto.LoginRequest;
import com.found404.marketbee.user.dto.LoginResponse;
import com.found404.marketbee.user.dto.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;


    @Transactional
    public void signup(SignUpRequest request) {
        // 사업자 진위 확인 실패
        if (!request.isVerified()) {
            throw new IllegalArgumentException("사업자 진위확인에 실패했습니다.");
        }

        // 필수 입력 누락
        if (request.getUserId() == null || request.getPassword() == null || request.getUserName() == null) {
            throw new IllegalArgumentException("필수 입력값이 누락되었습니다.");
        }

        // 비밀번호 형식 검증
        if (!request.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*]).{6,20}$")) {
            throw new IllegalArgumentException("비밀번호 형식이 올바르지 않습니다.");
        }

        // 이메일 형식 검증
        if (request.getEmail() == null ||
                !request.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(com|net|org|kr|co\\.kr|ac\\.kr|io|dev)$")) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }

        // 중복 아이디
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .userId(request.getUserId())
                .password(encodedPassword)
                .userName(request.getUserName())
                .email(request.getEmail())
                .verified(true)
                .build();

        Store store = new Store();
        store.setPlaceId(request.getPlaceId());
        store.setStoreName(request.getStoreName());
        store.setRoadAddress(request.getRoadAddress());
        store.setLongitude(BigDecimal.valueOf(request.getLongitude()));
        store.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        store.setUser(user);

        user.setStore(store);
        userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtProvider.createToken(user.getUserId());
        Store store = user.getStore();

        return LoginResponse.builder()
                .success(true)
                .message("로그인에 성공했습니다.")
                .accessToken(token)
                .storeName(store.getStoreName())
                .roadAddress(store.getRoadAddress())
                .build();
    }


    @Transactional(readOnly = true)
    public boolean checkDuplicateUserId(String userId) {
        return userRepository.existsByUserId(userId);
    }
}
