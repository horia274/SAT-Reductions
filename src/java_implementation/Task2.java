
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Task2z
 * You have to implement 4 methods:
 * readProblemData         - read the problem input and store it however you see fit
 * formulateOracleQuestion - transform the current problem instance into a SAT instance and write the oracle input
 * decipherOracleAnswer    - transform the SAT answer back to the current problem's answer
 * writeAnswer             - write the current problem's answer
 */
public class Task2 extends Task {
    private int numberOfFamilies;
    private int numberOfRelations;
    private int dimensionOfClique;
    private boolean[][] matrixOfRelations;

    private int numberOfVariables;

    private String oracleAnswer;
    private int[] familiesFromClique;

    @Override
    public void solve() throws IOException, InterruptedException {
        readProblemData();
        formulateOracleQuestion();
        askOracle();
        decipherOracleAnswer();
        writeAnswer();
    }

    /**
     * read the problem input (inFilename) and store
     * the data in the object's attributes
     * @throws IOException checks input-output errors
     */
    @Override
    public void readProblemData() throws IOException {
        Scanner scanner = new Scanner(new File(inFilename));
        numberOfFamilies = scanner.nextInt();
        numberOfRelations = scanner.nextInt();
        dimensionOfClique = scanner.nextInt();
        matrixOfRelations = new boolean[numberOfFamilies][numberOfFamilies];

        /* compute adjacent matrix of the given graph */
        for (int i = 0; i < numberOfRelations; i++) {
            int firstFamily = scanner.nextInt();
            int secondFamily = scanner.nextInt();

            matrixOfRelations[firstFamily - 1][secondFamily - 1] = true;
            matrixOfRelations[secondFamily - 1][firstFamily - 1] = true;
        }
    }

    /**
     * transform the current problem into a SAT problem and write it
     * (oracleInFilename) in a format understood by the oracle
     * @throws IOException checks input-output errors
     */
    @Override
    public void formulateOracleQuestion() throws IOException {
        PrintStream printStream = new PrintStream(oracleInFilename);

        /* compute number of variables */
        numberOfVariables = numberOfFamilies * dimensionOfClique;
        /* compute number of clauses */
        int numberOfClauses = dimensionOfClique
                + numberOfFamilies * dimensionOfClique * (dimensionOfClique - 1) / 2
                + (numberOfFamilies * (numberOfFamilies - 1) - numberOfRelations)
                * dimensionOfClique * dimensionOfClique;

        printStream.println("p cnf " + numberOfVariables + " " + numberOfClauses);

        /* clause type 1 */
        for (int i = 1; i <= dimensionOfClique; i++) {
            for (int j = 1; j <= numberOfFamilies; j++) {
                /* encoding variables */
                int FjPi = dimensionOfClique * (j - 1) + i;
                printStream.print(FjPi + " ");
            }
            printStream.println(0);
        }

        /* clause type 2 */
        for (int i = 1; i <= numberOfFamilies; i++) {
            for (int j = 1; j < dimensionOfClique; j++) {
                for (int k = j + 1; k <= dimensionOfClique; k++) {
                    int FiPj = dimensionOfClique * (i - 1) + j;
                    int FiPk = dimensionOfClique * (i - 1) + k;
                    printStream.println(-FiPj + " " + -FiPk + " " + 0);
                }
            }
        }

        /* clause type 3 */
        for (int i = 1; i < numberOfFamilies; i++) {
            for (int j = i + 1; j <= numberOfFamilies; j++) {
                if (!matrixOfRelations[i - 1][j - 1]) {
                    for (int k = 1; k <= dimensionOfClique; k++) {
                        for (int l = 1; l <= dimensionOfClique; l++) {
                            int FiPk = dimensionOfClique * (i - 1) + k;
                            int FjPl = dimensionOfClique * (j - 1) + l;
                            printStream.println(-FiPk + " " + -FjPl + " " + 0);
                        }
                    }
                }
            }
        }
    }

    /**
     * extract the current problem's answer from the answer
     * given by the oracle (oracleOutFilename)
     * @throws IOException checks input-output errors
     */
    @Override
    public void decipherOracleAnswer() throws IOException {
        Scanner scanner = new Scanner(new File(oracleOutFilename));

        oracleAnswer = scanner.nextLine();
        if (oracleAnswer.equals("True")) {
            familiesFromClique = new int[dimensionOfClique];
            numberOfVariables = scanner.nextInt();
            int counter = 0;
            for (int i = 1; i <= numberOfVariables; i++) {
                int currentVariable = scanner.nextInt();
                /* take the positive encoded variables */
                if (currentVariable > 0) {
                    int family;
                    /* decode them */
                    if (currentVariable % dimensionOfClique == 0) {
                        family = currentVariable / dimensionOfClique;
                    } else {
                        family = currentVariable / dimensionOfClique + 1;
                    }
                    /* store the families from clique */
                    familiesFromClique[counter++] = family;
                }
            }
        }
    }

    /**
     * write the answer to the current problem (outFilename)
     * @throws IOException checks input-output errors
     */
    @Override
    public void writeAnswer() throws IOException {
        PrintStream printStream = new PrintStream(outFilename);

        printStream.println(oracleAnswer);
        if (oracleAnswer.equals("True")) {
            StringBuilder stringBuilder = new StringBuilder();
            /* print families from clique */
            for (int i = 0; i < dimensionOfClique; i++) {
                stringBuilder.append(familiesFromClique[i]).append(" ");
            }
            printStream.println(stringBuilder.toString());
        }
    }
}
