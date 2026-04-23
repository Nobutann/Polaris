# Polaris Frontend (React + Vite)

Este é o frontend do projeto Polaris (SEBRAE), focado em fornecer uma leitura operacional e métricas analíticas sobre retenção e evasão de alunos.

## Modo de Demonstração (Mocks Locais)

O projeto contém um **Modo de Demonstração** isolado da API real, permitindo navegar pelo app e realizar apresentações sem precisar de um backend rodando, banco de dados ou ambiente de servidor.

**Para ativar o Modo Demo:**
1. Crie o arquivo `.env` na raiz da pasta `frontend/` usando o modelo:
   ```env
   VITE_DEMO_MODE=true
   ```
2. Inicie a aplicação com `npm run dev`.
3. Para voltar a consumir a API real, mude para `VITE_DEMO_MODE=false`.

**Arquitetura do Modo Demo:**
- Isola totalmente os mocks em arquivos específicos (`src/mocks/`), mantendo a lógica de componentes intocada e consumindo `src/services/dataService.js` de forma transparente.
- Preenche a aplicação com dados realistas, coesos e variados para validação visual e apresentação fluida.

## Integração com Backend Real
Quando configurado com `VITE_DEMO_MODE=false`, o app consome os endpoints originais (através de `src/services/dataService.js` delegando para `src/api.js`), interligados com os DTOs do backend (Java Spring Boot). A tipagem do frontend (no estilo JSDoc em `src/types/`) espelha perfeitamente `CourseMetricSnapshotDTO`, `CourseAggregateSummaryDTO`, e `SignalSummaryDTO`.

## Scripts

- **`npm run dev`**: Inicia o servidor local de desenvolvimento na porta configurada.
- **`npm run build`**: Gera um bundle pronto para produção.
- **`npm run lint`**: Executa as verificações do ESLint no código base.

## Bibliotecas Principais

- **React 19**
- **Vite 6**
- **Recharts**: Para os gráficos de retenção e timeline de evasão
