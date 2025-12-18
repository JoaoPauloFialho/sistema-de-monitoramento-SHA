# Comandos Rápidos - Sistema de Monitoramento

## Setup Inicial (Primeira vez)

```bash
cd /home/user/Projects/apps/hidrometro/MonitoringSystem

# 1. Baixar driver SQLite
./download_sqlite.sh

# 2. Compilar
./compile_gui.sh
```

## Execução Normal

```bash
cd /home/user/Projects/apps/hidrometro/MonitoringSystem

# Compilar e executar (tudo de uma vez)
./compile_gui.sh && ./run_gui.sh
```

## Comandos Individuais

```bash
# Compilar GUI
./compile_gui.sh

# Executar GUI
./run_gui.sh

# Compilar CLI
./compile.sh

# Executar CLI
./run.sh
```

## Estrutura de Pastas

```
MonitoringSystem/
├── lib/                    # Bibliotecas (sqlite-jdbc.jar deve estar aqui)
├── bin/                    # Classes compiladas
├── src/                    # Código fonte
├── monitoring.db           # Banco SQLite (criado automaticamente)
├── compile_gui.sh          # Script de compilação GUI
├── run_gui.sh              # Script de execução GUI
├── download_sqlite.sh      # Script para baixar SQLite
└── INSTRUCOES_SQLITE.md    # Instruções detalhadas
```

## Troubleshooting

### Erro: "SQLite JDBC driver not found" ou "ClassNotFoundException: org.slf4j.LoggerFactory"
```bash
./download_sqlite.sh
# O script baixa automaticamente o SQLite JDBC e as dependências SLF4J
```

### Erro: "ClassNotFoundException"
- Verifique se `sqlite-jdbc-*.jar` está em `lib/`
- Recompile: `./compile_gui.sh`

### Resetar banco de dados
```bash
rm monitoring.db
# O banco será recriado na próxima execução
```

