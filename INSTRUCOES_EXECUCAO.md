# Instruções de Execução

Você precisará de **3 terminais** abertos.

## Terminal 1: Simulador Rodrigues
Este simulador gera imagens de leitura de hidrômetro.

```bash
cd /home/user/Projects/apps/hidrometro/rodrigues-hidrometro/Simulador-Hidrometro
javac -d bin -sourcepath src src/main/java/br/com/simulador/Main.java
java -cp bin main.java.br.com.simulador.Main
```

## Terminal 2: Simulador João Paulo
Este é o segundo simulador.

```bash
cd /home/user/Projects/apps/hidrometro/joaopaulo-hidrometro/hidrometro
javac -d bin -sourcepath src src/main/java/br/hidrometro/Main.java
java -cp bin main.java.br.hidrometro.Main
```

## Terminal 3: Sistema de Monitoramento (Seu Projeto)
Este é o app principal que você vai usar.

1. Compile o projeto:
```bash
cd /home/user/Projects/apps/hidrometro/MonitoringSystem
javac -d bin -cp src src/br/com/monitoring/ui/gui/SwingMain.java src/br/com/monitoring/Main.java
```

2. Execute a Interface Gráfica:
```bash
java -cp bin br.com.monitoring.ui.gui.SwingMain
```

### Como Utilizar
1. No **Sistema de Monitoramento**, vá na aba "Users".
2. Preencha os dados do usuário.
3. No campo "Assign Meter", selecione um dos medidores detectados (ROD ou JOAO).
   - *Nota: Certifique-se que os simuladores já rodaram pelo menos uma vez para criar as pastas.*
4. Clique em "Register User".
5. Vá para o "Dashboard".
6. Conforme novos arquivos são gerados nos terminais dos simuladores, o Dashboard atualizará automaticamente a cada 3 segundos.
