# Instruções de Configuração e Execução

## 1. Baixar o Driver SQLite e Dependências

Antes de compilar, você precisa baixar o driver JDBC do SQLite e suas dependências (SLF4J):

```bash
cd /home/user/Projects/apps/hidrometro/MonitoringSystem
./download_sqlite.sh
```

O script baixa automaticamente:
- `sqlite-jdbc-3.44.1.0.jar` - Driver SQLite JDBC
- `slf4j-api-2.0.9.jar` - API do SLF4J (requerido pelo SQLite)
- `slf4j-simple-2.0.9.jar` - Implementação simples do SLF4J

Todos os arquivos serão salvos em `lib/`.

## 2. Compilar o Projeto

### Para Interface Gráfica (GUI):
```bash
cd /home/user/Projects/apps/hidrometro/MonitoringSystem
./compile_gui.sh
```

### Para Interface CLI:
```bash
cd /home/user/Projects/apps/hidrometro/MonitoringSystem
./compile.sh
```

## 3. Executar o Sistema

### Interface Gráfica (Recomendado):
```bash
./run_gui.sh
```

### Interface CLI:
```bash
./run.sh
```

## 4. Banco de Dados SQLite

O sistema agora usa SQLite para persistência. O banco de dados será criado automaticamente como `monitoring.db` na pasta do projeto.

### Estrutura do Banco:
- **users**: Armazena usuários (cpf, name, address, consumption_limit)
- **meters**: Armazena medidores (id, location, user_cpf, last_processed_image)
- **consumption_records**: Histórico de leituras (meter_id, value, timestamp, image_path)

### Vantagens:
- ✅ Dados persistem entre execuções
- ✅ Não precisa recadastrar usuários toda vez
- ✅ Histórico completo de consumo
- ✅ Banco de dados local (não precisa servidor)

## 5. Workflow Completo

1. **Terminal 1**: Rode o simulador Rodrigues
   ```bash
   cd /home/athavus/Projects/apps/hidrometro/rodrigues-hidrometro/Simulador-Hidrometro
   javac -d bin -sourcepath src src/main/java/br/com/simulador/Main.java
   java -cp bin main.java.br.com.simulador.Main
   ```

2. **Terminal 2**: Rode o simulador João Paulo
   ```bash
   cd /home/user/Projects/apps/hidrometro/joaopaulo-hidrometro/hidrometro
   javac -d bin -sourcepath src src/main/java/br/hidrometro/Main.java
   java -cp bin main.java.br.hidrometro.Main
   ```

3. **Terminal 3**: Rode o Sistema de Monitoramento
   ```bash
   cd /home/user/Projects/apps/hidrometro/MonitoringSystem
   ./compile_gui.sh
   ./run_gui.sh
   ```

4. **No Sistema de Monitoramento**:
   - Vá em "Create" para cadastrar usuários
   - Os dados serão salvos automaticamente no SQLite
   - Vá em "Dashboard" para ver os usuários e status
   - O sistema monitora automaticamente a cada 3 segundos

## 6. Comandos Rápidos

```bash
# Compilar e executar GUI (tudo de uma vez)
cd /home/user/Projects/apps/hidrometro/MonitoringSystem
./compile_gui.sh && ./run_gui.sh
```

## Notas

- O banco de dados `monitoring.db` será criado automaticamente na primeira execução
- Para resetar o banco, simplesmente delete o arquivo `monitoring.db`
- O driver SQLite precisa estar na pasta `lib/` antes de compilar

