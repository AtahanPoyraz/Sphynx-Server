package io.sphynx.server.service;

import io.sphynx.server.repository.AgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentTokenService {
    private final AgentRepository agentRepository;
    // TODO: tek kullanımlık, süreli (15dk) ve ajan uuid sini saklayacak bi token olusturulacak uuid parse edilecek validasyon yapılacak

    @Autowired
    public AgentTokenService(
            AgentRepository agentRepository
    ) {
        this.agentRepository = agentRepository;
    }
}
