# Sharingan (NeoForge 1.21.1)

Mod que adiciona somente o Sharingan do Naruto: 1, 2 e 3 tomoe, Mangekyou (Itachi, Sasuke, Obito, Shisui) e Mangekyou Eterno.

## Compilar no GitHub
1. Crie um repositorio e envie o conteudo desta pasta (incluindo `.github`).
2. Aba **Actions** > workflow **Build mod** (roda sozinho a cada push).
3. Baixe o `.jar` em **Artifacts > sharingan-mod**. Coloque em `mods/` com NeoForge 21.1.x.

## Teclas (reconfiguraveis em Controles > Sharingan)
| Tecla | Acao |
|---|---|
| J | Aba Sharingan (equipar/remover, extrair olho, partilhar Obito) |
| V | Ativar/desativar Sharingan |
| B | Ativar/desativar Mangekyou |
| \ | Habilidade 1 |
| Z | Habilidade 2 |
| X | Habilidade 3 |
| C | Habilidade 4 (Susanoo) |

## Progressao
- **Despertar**: sobreviver a um golpe que deixe voce com 2 coracoes ou menos (30% de chance).
- **1 > 2 tomoe**: 200 xp. **2 > 3 tomoe**: 600 xp. O xp so conta com o Sharingan ativo: +10 por monstro morto, + xp de orbes coletados, +1 a cada 2 segundos ativo.
- **Mangekyou**: com 3 tomoe, mate um mob domesticado. Lobo = Itachi, Papagaio = Sasuke, Cavalo/similares = Obito, Gato = Shisui (outros domesticados = Itachi).
- **Eterno**: com o Mangekyou de Itachi ou Sasuke, implante o olho do outro (clique direito no item). Restaura a visao e remove o desgaste.
- **Obito**: na aba J, "Partilhar" parte o olho ao meio (voce fica com meio olho; o item e o outro meio). Quem usa meio olho tem recargas +50% e desgaste -50%.

## Efeitos passivos
| Nivel | Efeitos | Ticks tirados dos inimigos/projeteis proximos |
|---|---|---|
| 1 tomoe | Visao noturna | 2 |
| 2 tomoe | + Velocidade | 3 |
| 3 tomoe | + Forca | 4 |
| Mangekyou | + Resistencia, Forca II | 5 |
| Eterno | Resistencia II | 6 |

"Tirar ticks" = mobs e projeteis de outros (raio 10) se movem a (20 - N)/20 da velocidade normal.

## Habilidades
- **Itachi**: Tsukuyomi, Amaterasu (chamas negras), Karasu Bunshin.
- **Sasuke**: Chidori Corrente (investida), Enton Kagutsuchi, Kirin (raios).
- **Obito**: Kamui intangibilidade, Kamui teletransporte, Kamui succao.
- **Shisui**: Kotoamatsukami (controla mob / paralisa jogador), Shunshin, Olhar Revelador.
- **Eterno**: Tsukuyomi, Amaterasu, Kirin + Susanoo Perfeito.
- **Susanoo** (tecla C): armadura de chakra com absorcao, resistencia forte e dano em area.

## Cegueira
Cada habilidade e o uso do Mangekyou aumentam o **Desgaste ocular** (barra no HUD). Ao chegar em 100% voce fica **cego**: tela preta. Ativando o Sharingan (V) voce enxerga apenas silhuetas escuras. So o Mangekyou Eterno cura.

## Comandos de teste (OP)
`/sharingan stage <0-3>`, `/sharingan mangekyou <itachi|sasuke|obito|shisui|eterno>`, `/sharingan xp <n>`, `/sharingan blind`, `/sharingan cure`
