package br.edu.ifpb.ifmeetup.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.ifmeetup.domain.repository.auth.RoleRepository;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DataLoader {

    private final DataSource dataSource;
    private final RoleRepository roleRepository;

    @PostConstruct
    @Transactional
    public void loadData() {
        if (roleRepository.count() > 0) {
            log.info("Roles já estão cadastradas no sistema. Pulando inicialização de dados.");
            return;
        }

        log.info("Inicializando dados básicos do sistema (roles e permissões)...");
        
        try {
            ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
            resourceDatabasePopulator.addScript(new ClassPathResource("db/data.sql"));
            resourceDatabasePopulator.setContinueOnError(true);
            resourceDatabasePopulator.execute(dataSource);
            
            log.info("Dados básicos carregados com sucesso!");
        } catch (ScriptException e) {
            log.error("Erro ao executar script SQL: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro ao carregar dados iniciais: {}", e.getMessage(), e);
        }
    }
} 