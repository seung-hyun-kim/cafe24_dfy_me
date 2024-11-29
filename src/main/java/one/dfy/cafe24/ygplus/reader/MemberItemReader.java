package one.dfy.cafe24.ygplus.reader;

import lombok.extern.slf4j.Slf4j;
import one.dfy.cafe24.ygplus.buffz.dto.Member;
import one.dfy.cafe24.ygplus.buffz.mapper.BuffzMapper;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MemberItemReader {

    @Autowired
    private BuffzMapper memberMapper;

    private List<Member> members;
    private int index = 0;

    // ItemReader 메서드
    @Bean
    @StepScope
    public ItemReader<Member> reader() {

        //정기배송 대상자 축출
        List<Member> members = memberMapper.findMembersToDowngrade();
        if (members == null) {
            log.info("정기배송 대상자 is null");
        } else {
            log.info("정기배송 대상자 size: " + members.size());
        }
        return new ItemReader<Member>() {
            @Override
            public Member read() {
                if (index < members.size()) {
                    return members.get(index++);
                }
                return null; // 데이터가 없으면 null 반환
            }
        };
    }
}

