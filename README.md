# Calypte

Calypte é um sistema de cache de propósito geral com suporte transacional. 
Permite o armazenamento de dados na forma de chave-valor em memoria e disco. 
É extremamente rápido, tanto para escrita como para leitura, podendo chegar a mais de 600.000 operações por segundo. Não é necessária grandes quantidades de memória para seu funcionamento. Ele trabalha de forma eficiente com pouca memória.

# Como instalar?

1. git clone https://github.com/calyptelabs/calypte.git
2. mvn clean install
3. Adicionar no pom.xml:
 ```
 <dependency>
     <groupId>calypte</groupId>
     <artifactId>calypte</artifactId>
     <version>1.0.2.0</version>
 </dependency>
 ```
