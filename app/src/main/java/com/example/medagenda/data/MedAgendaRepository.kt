package com.example.medagenda.data

import com.example.medagenda.model.Medico
import com.example.medagenda.model.Paciente
import com.example.medagenda.model.TipoUsuario
import com.example.medagenda.model.UsuarioLogado

/**
 * REPOSITÓRIO: o "banco de dados" do app.
 *
 * O que é: o ÚNICO lugar do app que guarda e busca dados.
 * As telas nunca mexem nos dados direto: elas sempre pedem para o repositório.
 *
 * POR ENQUANTO os dados ficam em listas na memória do celular
 * (por isso somem quando o app é fechado).
 * QUANDO A API FICAR PRONTA, só o "recheio" destas funções muda para chamar a
 * API (com Retrofit). As telas continuam iguais, porque só conhecem estas funções.
 * Essa é a vantagem de centralizar tudo aqui.
 *
 * Por que `object` e não `class`: `object` significa que existe UM só
 * repositório no app inteiro. Assim, o paciente cadastrado na tela de cadastro
 * é o mesmo que a tela de login encontra depois.
 *
 * Rotas previstas para a API (combinadas com o grupo):
 *  - POST /pacientes  -> cadastrarPaciente   (400 campo vazio, 409 e-mail repetido)
 *  - GET  /pacientes  -> listarPacientes
 *  - POST /medicos    -> cadastrarMedico     (400 campo vazio, 409 e-mail ou CRM repetido)
 *  - GET  /medicos    -> listarMedicos
 *  - POST /login      -> login               (401 e-mail ou senha incorretos)
 *
 * Por que as funções são `suspend`: chamar uma API demora, e o Android não deixa
 * a tela "travar" esperando. Uma função `suspend` pode esperar sem travar a tela.
 * Hoje a resposta é instantânea, mas já deixamos assim para a API encaixar sem
 * precisar mudar as telas.
 *
 * Liga com: LoginActivity, CadastroPacienteActivity, CadastroMedicoActivity e PrincipalActivity.
 */
object MedAgendaRepository {

    // "TABELA" DE PACIENTES: uma lista que pode crescer (mutableList).
    // Já começa com um paciente de teste, para conseguir fazer login logo de cara.
    // `private` = só o repositório mexe nesta lista; as telas não têm acesso direto.
    private val pacientes = mutableListOf(
        Paciente(1, "João Silva", "54999990000", "teste@email.com", "123456")
    )

    // "TABELA" DE MÉDICOS: separada da de pacientes, como pedido no projeto.
    // Também começa com um médico de teste.
    private val medicos = mutableListOf(
        Medico(1, "Carlos Santos", "54988880000", "medico@email.com", "12345-RS", "Cardiologia", "123456")
    )

    // QUEM ESTÁ LOGADO AGORA. `null` significa que ninguém está logado.
    // `private set` = qualquer tela pode LER, mas só o repositório pode MUDAR
    // (no login e no logout). Isso evita que uma tela "logue" alguém por engano.
    var usuarioLogado: UsuarioLogado? = null
        private set

    // Verifica se o e-mail já existe em QUALQUER uma das duas tabelas.
    // Por que nas duas: a mesma pessoa não pode ter o mesmo e-mail como médico e
    // como paciente, senão o login não saberia qual conta abrir.
    // `ignoreCase = true` = trata "Teste@Email.com" e "teste@email.com" como iguais.
    private fun emailJaExiste(email: String): Boolean =
        pacientes.any { it.email.equals(email, ignoreCase = true) } ||
            medicos.any { it.email.equals(email, ignoreCase = true) }

    /**
     * CADASTRAR PACIENTE (futuro: POST /pacientes)
     * Chamada pela CadastroPacienteActivity quando o usuário toca em "Cadastrar".
     */
    suspend fun cadastrarPaciente(paciente: Paciente): Resultado<Paciente> {
        // 1) Se o e-mail já existe, não cadastra e devolve o erro para a tela mostrar.
        if (emailJaExiste(paciente.email)) return Resultado.Erro("Este e-mail já está cadastrado.")

        // 2) Gera o próximo id: pega o maior id da lista e soma 1.
        //    (`?: 0` = se a lista estiver vazia, considera o maior como 0.)
        //    `copy` cria uma cópia do paciente igual, só que com o id novo.
        val novo = paciente.copy(id = (pacientes.maxOfOrNull { it.id } ?: 0) + 1)

        // 3) Salva na "tabela" de pacientes e avisa a tela que deu certo.
        pacientes.add(novo)
        return Resultado.Sucesso(novo)
    }

    /**
     * CADASTRAR MÉDICO (futuro: POST /medicos)
     * Chamada pela CadastroMedicoActivity. Igual ao paciente, mas também confere o CRM.
     */
    suspend fun cadastrarMedico(medico: Medico): Resultado<Medico> {
        // 1) E-mail repetido? Não cadastra.
        if (emailJaExiste(medico.email)) return Resultado.Erro("Este e-mail já está cadastrado.")

        // 2) CRM repetido? Não cadastra. Cada médico tem um CRM único.
        if (medicos.any { it.crm.equals(medico.crm, ignoreCase = true) }) {
            return Resultado.Erro("Este CRM já está cadastrado.")
        }

        // 3) Gera o id e salva na "tabela" de médicos.
        val novo = medico.copy(id = (medicos.maxOfOrNull { it.id } ?: 0) + 1)
        medicos.add(novo)
        return Resultado.Sucesso(novo)
    }

    // LISTAR (futuro: GET /pacientes e GET /medicos). Servem para consultar as tabelas.
    // Ainda não são usadas por nenhuma tela; ficam prontas para as próximas telas
    // (ex.: lista de médicos para agendar consulta).
    // `toList()` devolve uma CÓPIA, para ninguém de fora alterar a lista original.
    suspend fun listarPacientes(): List<Paciente> = pacientes.toList()

    suspend fun listarMedicos(): List<Medico> = medicos.toList()

    /**
     * LOGIN (futuro: POST /login)
     * Chamada pela LoginActivity quando o usuário toca em "Entrar".
     *
     * Como funciona:
     *  1) Procura na tabela de MÉDICOS alguém com esse e-mail e essa senha.
     *  2) Se não achar, procura na tabela de PACIENTES.
     *  3) Se não achar em nenhuma, devolve erro.
     * Assim, a mesma tela de login serve para os dois tipos de usuário.
     */
    suspend fun login(email: String, senha: String): Resultado<UsuarioLogado> {
        // `find` = procura o primeiro da lista que atende a condição (ou null se não achar).
        // `?.let { ... }` = SE achou, transforma em UsuarioLogado (sem a senha).
        // `?:` = "se não achou, tenta o próximo". O último `?:` desiste e devolve o erro.
        val usuario = medicos.find { it.email.equals(email, true) && it.senha == senha }
            ?.let { UsuarioLogado(it.id, it.nome, it.email, TipoUsuario.MEDICO) }
            ?: pacientes.find { it.email.equals(email, true) && it.senha == senha }
                ?.let { UsuarioLogado(it.id, it.nome, it.email, TipoUsuario.PACIENTE) }
            ?: return Resultado.Erro("E-mail ou senha incorretos.")

        // Guarda quem entrou, para a tela principal saber o nome e o tipo.
        usuarioLogado = usuario
        return Resultado.Sucesso(usuario)
    }

    // LOGOUT: "esquece" quem estava logado. Chamado pelo botão "Sair" da tela principal.
    fun logout() {
        usuarioLogado = null
    }
}
