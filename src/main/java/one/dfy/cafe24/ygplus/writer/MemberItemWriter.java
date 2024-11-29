package one.dfy.cafe24.ygplus.writer;

import one.dfy.cafe24.ygplus.buffz.dto.Member;
import one.dfy.cafe24.ygplus.buffz.mapper.BuffzMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberItemWriter implements ItemWriter<List<Member>>{

    private static final Logger log = LoggerFactory.getLogger(MemberItemWriter.class);
    private final BuffzMapper buffzMapper;

    public MemberItemWriter(BuffzMapper buffzMapper) {
        this.buffzMapper = buffzMapper;
    }

    @Override
    public void write(Chunk<? extends List<Member>> chunk) throws Exception {

        for (List<Member> members : chunk.getItems()) {
            for (Member member : members) {
                // DB에 member 정보 갱신
                buffzMapper.updateMember(member); // memberDTO로 변경
            }
        }

    }
}

