
package core.view;

import core.model.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.util.ArrayList;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import core.controller.MegaferiaController;
import core.controller.ServiceResponse;
import java.util.Arrays;
import java.util.List;
import core.controller.utils.Observer;
import core.controller.utils.Observable;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;


public class MegaferiaFrame extends javax.swing.JFrame implements Observer {
    
    private final MegaferiaController controller;
    /**
     * Creates new form MegaferiaFrame
     */
    public MegaferiaFrame(MegaferiaController controller) {
        this.controller = controller;
        initComponents();
        setLocationRelativeTo(null);
        // REGISTRO CLAVE: La Vista se registra como Observador del Controlador
        this.controller.addObserver(this); // <-- AÑADIR ESTA LÍNEA
        
        // Lógica inicial para cargar tablas al inicio (por defecto)
        this.update(); // Carga inicial

    }
    
    private void loadComboBoxes() {
        // 1. Limpiar todos los ComboBoxes clave
        cmbManagerReg.removeAllItems();
        cmbPublisherBookReg.removeAllItems();
        cmbPublisherStandAssign.removeAllItems();
        cmbNarratorReg.removeAllItems();
        cmbStandIDSelect.removeAllItems();
        cmbBookAuthorSelect.removeAllItems();
        cmbAuthorSearch.removeAllItems();

        // 2. Llenar con datos frescos

        // Gerentes (cmbManagerReg)
        controller.getManagers().forEach(m -> cmbManagerReg.addItem(m.getId() + " - " + m.getFullname()));

        // Editoriales (cmbPublisherBookReg, cmbPublisherStandAssign)
        controller.getPublishers().forEach(p -> {
            String item = p.getNit() + " - " + p.getName();
            cmbPublisherBookReg.addItem(item);
            cmbPublisherStandAssign.addItem(item);
        });

        // Autores (cmbBookAuthorSelect, cmbAuthorSearch)
        controller.getAuthors().forEach(a -> {
            String item = a.getId() + " - " + a.getFullname();
            cmbBookAuthorSelect.addItem(item);
            cmbAuthorSearch.addItem(item);
        });

        // Narradores (cmbNarratorReg)
        controller.getNarrators().forEach(n -> cmbNarratorReg.addItem(n.getId() + " - " + n.getFullname()));

        // Stands (cmbStandIDSelect - para el carrito)
        controller.getStands().forEach(s -> cmbStandIDSelect.addItem(String.valueOf(s.getId())));
        }
    
    
    // Metodos helper loadTable
    private void loadPublishersTable() {
        DefaultTableModel model = (DefaultTableModel) tblPublishers.getModel(); 
        model.setRowCount(0); 

        controller.getPublishers().forEach(p -> {
            Object[] rowData = {
                p.getNit(),
                p.getName(),
                p.getAddress(),
                p.getManager().getFullname(),
                p.getStandQuantity()
            };
            model.addRow(rowData);
        });
    }
    
    private void loadPeopleTable() {
        DefaultTableModel model = (DefaultTableModel) tblPeople.getModel(); 
        model.setRowCount(0); 
        // Muestra Gerentes, Autores y Narradores en la misma tabla
        // Gerentes (Tipo: Gerente, Editorial: Nombre de la Editorial que maneja, Nro. Libros: 0)
        controller.getManagers().forEach(m -> {
            String publisherName = m.getPublisher() != null ? m.getPublisher().getName() : "N/A";
            model.addRow(new Object[]{
                m.getId(), 
                m.getFullname(), 
                "Gerente", 
                publisherName, // Editorial
                0 // Nro. Libros
            });
        });

        // Autores (Tipo: Autor, Editorial: N/A, Nro. Libros: bookQuantity)
        controller.getAuthors().forEach(a -> {
            model.addRow(new Object[]{
                a.getId(), 
                a.getFullname(), 
                "Autor", 
                "N/A", // Editorial (El autor no está ligado a una sola)
                a.getBookQuantity() // Nro. Libros
            });
        });

        // Narradores (Tipo: Narrador, Editorial: N/A, Nro. Libros: bookQuantity)
        controller.getNarrators().forEach(n -> {
            model.addRow(new Object[]{
                n.getId(), 
                n.getFullname(), 
                "Narrador", 
                "N/A", // Editorial
                n.getBookQuantity() // Nro. Libros
            });
        });
    }
    
    private void loadTopAuthorsTable() {
    DefaultTableModel model = (DefaultTableModel) tblTopAuthors.getModel();
    model.setRowCount(0);

    // CAMBIO: Recibimos un Map<Author, Long>
    ServiceResponse<Map<Author, Long>> response = controller.getTopAuthorsByPublisherDiversity();

    if (response.isSuccess()) {
        // Iteramos sobre el Mapa (Clave: Autor, Valor: Cantidad)
        response.getData().forEach((author, count) -> {
            model.addRow(new Object[]{
                author.getId(), 
                author.getFullname(), 
                count // <--- ¡AHORA SÍ TENEMOS EL NÚMERO PARA LA 3ra COLUMNA!
            });
        });
    }
    }
    
    private void loadStandsTable() {
    // 1. Obtener el modelo de la tabla de Stands
    DefaultTableModel model = (DefaultTableModel) tblStands.getModel(); 
    
    // 2. Limpiar filas existentes
    model.setRowCount(0); 

    // 3. Llenar con datos frescos
    controller.getStands().forEach(s -> {
        // Se asume la estructura de la tabla: ID, Precio, Editoriales Asignadas
        String publisherNames = s.getPublishers().stream()
                                  .map(p -> p.getName())
                                  .collect(Collectors.joining(", "));
        
        Object[] rowData = {
            s.getId(),
            s.getPrice(),
            s.getPublisherQuantity(), // Cantidad de editoriales (usando el método del UML)
            publisherNames.isEmpty() ? "N/A" : publisherNames // Nombres de las editoriales
        };
        model.addRow(rowData);
    });
    }
    
    private void loadBooksTable() {
    // 1. Obtener el modelo de la tabla de Libros
    DefaultTableModel model = (DefaultTableModel) tblBooks.getModel(); 
    
    // 2. Limpiar filas existentes
    model.setRowCount(0); 

    // 3. Llenar con datos frescos
    controller.getBooks().forEach(b -> {
        
        // Convertir la lista de Autores en una cadena separada por comas
        String authorList = b.getAuthors().stream()
                              .map(a -> a.getFullname())
                              .collect(Collectors.joining(", "));
        
        // Se asume la estructura de la tabla: Título, Autores, ISBN, Género, Formato, Valor, Editorial
        Object[] rowData = {
            b.getTitle(),
            authorList,
            b.getIsbn(),
            b.getGenre(),
            b.getFormat(),
            b.getValue(),
            b.getPublisher().getName() // Asumiendo que Book tiene un getPublisher()
        };
        model.addRow(rowData);
    });
    }
    
    // Método helper para llenar la tabla de resultados de búsqueda
    private void fillSearchResultsTable(List<Book> books) {
        DefaultTableModel model = (DefaultTableModel) tblSearchResults.getModel();
        model.setRowCount(0);

        books.forEach(book -> {
            String authors = book.getAuthors().stream()
                    .map(Author::getFullname)
                    .collect(Collectors.joining(", "));

            String narrator = "N/A";
            String duration = "N/A";
            String hyperlink = "N/A";
            String pages = "N/A";
            String copies = "N/A";

            if (book instanceof Audiobook ab) {
                narrator = ab.getNarrator().getFullname();
                duration = String.valueOf(ab.getDuration());
            } else if (book instanceof DigitalBook db) {
                hyperlink = db.getHyperlink();
            } else if (book instanceof PrintedBook pb) {
                pages = String.valueOf(pb.getPages());
                copies = String.valueOf(pb.getCopies());
            }

            model.addRow(new Object[]{
                book.getTitle(),
                authors,
                book.getIsbn(),
                book.getGenre(),
                book.getFormat(),
                book.getValue(),
                book.getPublisher().getName(),
                copies,
                pages,
                hyperlink,
                narrator,
                duration
            });
        });
    }
    
    private void performSearch() {
        // 1. Obtener criterios
        String authorItem = (String) cmbAuthorSearch.getSelectedItem();
        String formatItem = (String) cmbFormatSearch.getSelectedItem();

        String authorId = null;
        if (authorItem != null && authorItem.contains(" - ")) {
            authorId = authorItem.split(" - ")[0].trim();
        }

        boolean hasAuthor = (authorId != null && !authorId.isEmpty());
        boolean hasFormat = (formatItem != null && !formatItem.equals("Seleccione uno..."));

        ServiceResponse<List<Book>> response = null;

        // 2. Decidir qué búsqueda ejecutar
        if (hasAuthor && hasFormat) {
            // AMBOS: Búsqueda combinada
            response = controller.searchBooksByAuthorAndFormat(authorId, formatItem);
        } else if (hasAuthor) {
            // SOLO AUTOR
            response = controller.searchBooksByAuthor(authorId);
        } else if (hasFormat) {
            // SOLO FORMATO
            response = controller.searchBooksByFormat(formatItem);
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione al menos un criterio (Autor o Formato).", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Manejar Resultado
        if (response.isSuccess()) {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Búsqueda Exitosa", JOptionPane.INFORMATION_MESSAGE);
            fillSearchResultsTable(response.getData());
        } else {
            // Limpiar tabla si no hay resultados
            ((DefaultTableModel) tblSearchResults.getModel()).setRowCount(0);
            JOptionPane.showMessageDialog(this, response.getMessage(), "Sin Resultados", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    @Override
    public void update() {
    // 1. Recargar todos los ComboBoxes (la parte más importante de la actualización)
    loadComboBoxes(); 
    // 2. Recargar las Tablas de visualización principal
    loadPublishersTable(); 
    loadPeopleTable();   
    loadStandsTable();   
    loadBooksTable();    
    loadTopAuthorsTable();
}
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelStand = new javax.swing.JPanel();
        jLabelStandPrice = new javax.swing.JLabel();
        jLabelStandID = new javax.swing.JLabel();
        txtStandPrice = new javax.swing.JTextField();
        txtStandID = new javax.swing.JTextField();
        btnRegisterStand = new javax.swing.JButton();
        jPanelPerson = new javax.swing.JPanel();
        jLabelPersonName = new javax.swing.JLabel();
        jLabelPersonID = new javax.swing.JLabel();
        txtPersonID = new javax.swing.JTextField();
        txtPersonFirstName = new javax.swing.JTextField();
        btnRegisterAuthor = new javax.swing.JButton();
        txtPersonLastName = new javax.swing.JTextField();
        jLabelPersonLastName = new javax.swing.JLabel();
        btnRegisterManager = new javax.swing.JButton();
        btnRegisterNarrator = new javax.swing.JButton();
        jPanelPublisher = new javax.swing.JPanel();
        jLabelPublisherNIT = new javax.swing.JLabel();
        txtPublisherNIT = new javax.swing.JTextField();
        txtPublisherName = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtPublisherAddress = new javax.swing.JTextField();
        btnRegisterPublisher = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        cmbManagerReg = new javax.swing.JComboBox<>();
        jPanelBook = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        txtBookTitle = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtBookISBN = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        cmbBookGenre = new javax.swing.JComboBox<>();
        btnRegisterBook = new javax.swing.JButton();
        cmbBookAuthorSelect = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        rbPrintedBook = new javax.swing.JRadioButton();
        rbDigitalBook = new javax.swing.JRadioButton();
        rbAudiobook = new javax.swing.JRadioButton();
        jLabel15 = new javax.swing.JLabel();
        cmbBookFormatSearch = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();
        txtBookValue = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        cmbPublisherBookReg = new javax.swing.JComboBox<>();
        jLabel18 = new javax.swing.JLabel();
        txtPrintedBookPages = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        txtPrintedBookCopies = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        txtDigitalBookHyperlink = new javax.swing.JTextField();
        txtAudiobookDuration = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        cmbNarratorReg = new javax.swing.JComboBox<>();
        btnAddAuthorToBook = new javax.swing.JButton();
        btnRemoveAuthorFromBook = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAreaAuthorIDs = new javax.swing.JTextArea();
        jPanelStandPurchase = new javax.swing.JPanel();
        cmbStandIDSelect = new javax.swing.JComboBox<>();
        cmbPublisherStandAssign = new javax.swing.JComboBox<>();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        btnAddPublisherToStand = new javax.swing.JButton();
        btnConfirmStandAssignment = new javax.swing.JButton();
        btnRemovePublisherFromStand = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtAreaPublisherList = new javax.swing.JTextArea();
        btnAddStandToPurchase = new javax.swing.JButton();
        btnRemoveStandFromPurchase = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtAreaStandIDs = new javax.swing.JTextArea();
        jPanelShowPublishers = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblPublishers = new javax.swing.JTable();
        btnLoadPublishersTable = new javax.swing.JButton();
        jPanelShowPersons = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblPeople = new javax.swing.JTable();
        btnLoadPeopleTable = new javax.swing.JButton();
        jPanelShowStands = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblStands = new javax.swing.JTable();
        btnLoadStandsTable = new javax.swing.JButton();
        jPanelShowBooks = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tblBooks = new javax.swing.JTable();
        btnSearchBooksByType = new javax.swing.JButton();
        cmbBookSearch = new javax.swing.JComboBox<>();
        jLabel25 = new javax.swing.JLabel();
        jPanelAditionalQueries = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        cmbAuthorSearch = new javax.swing.JComboBox<>();
        jLabel27 = new javax.swing.JLabel();
        btnSearchBooksByAuthor = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        tblSearchResults = new javax.swing.JTable();
        jLabel28 = new javax.swing.JLabel();
        cmbFormatSearch = new javax.swing.JComboBox<>();
        btnSearchBooksByFormat = new javax.swing.JButton();
        jLabel29 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        tblTopAuthors = new javax.swing.JTable();
        btnSearchMaxPublishersAuthor = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanelStand.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelStandPrice.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabelStandPrice.setText("Precio");
        jPanelStand.add(jLabelStandPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 220, -1, -1));

        jLabelStandID.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabelStandID.setText("ID");
        jPanelStand.add(jLabelStandID, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 180, -1, -1));

        txtStandPrice.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtStandPrice.setToolTipText("");
        txtStandPrice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtStandPriceActionPerformed(evt);
            }
        });
        jPanelStand.add(txtStandPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 220, 150, 30));

        txtStandID.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtStandID.setToolTipText("");
        txtStandID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtStandIDActionPerformed(evt);
            }
        });
        jPanelStand.add(txtStandID, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 180, 150, 30));

        btnRegisterStand.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnRegisterStand.setText("Crear");
        btnRegisterStand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterStandActionPerformed(evt);
            }
        });
        jPanelStand.add(btnRegisterStand, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 280, 90, 40));

        jTabbedPane1.addTab("Stand", jPanelStand);

        jLabelPersonName.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabelPersonName.setText("Nombre");

        jLabelPersonID.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabelPersonID.setText("ID");

        txtPersonID.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtPersonID.setToolTipText("");
        txtPersonID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPersonIDActionPerformed(evt);
            }
        });

        txtPersonFirstName.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtPersonFirstName.setToolTipText("");
        txtPersonFirstName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPersonFirstNameActionPerformed(evt);
            }
        });

        btnRegisterAuthor.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnRegisterAuthor.setText("Crear Autor");
        btnRegisterAuthor.setName("btnRegisterAuthor"); // NOI18N
        btnRegisterAuthor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterAuthorActionPerformed(evt);
            }
        });

        txtPersonLastName.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtPersonLastName.setToolTipText("");
        txtPersonLastName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPersonLastNameActionPerformed(evt);
            }
        });

        jLabelPersonLastName.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabelPersonLastName.setText("Apellido");

        btnRegisterManager.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnRegisterManager.setText("Crear Gerente");
        btnRegisterManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterManagerActionPerformed(evt);
            }
        });

        btnRegisterNarrator.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnRegisterNarrator.setText("Crear Narrador");
        btnRegisterNarrator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterNarratorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelPersonLayout = new javax.swing.GroupLayout(jPanelPerson);
        jPanelPerson.setLayout(jPanelPersonLayout);
        jPanelPersonLayout.setHorizontalGroup(
            jPanelPersonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPersonLayout.createSequentialGroup()
                .addGroup(jPanelPersonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPersonLayout.createSequentialGroup()
                        .addGap(264, 264, 264)
                        .addGroup(jPanelPersonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelPersonLayout.createSequentialGroup()
                                .addComponent(jLabelPersonLastName)
                                .addGap(21, 21, 21)
                                .addComponent(txtPersonLastName, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelPersonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelPersonLayout.createSequentialGroup()
                                    .addComponent(jLabelPersonID)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtPersonID, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelPersonLayout.createSequentialGroup()
                                    .addComponent(jLabelPersonName)
                                    .addGap(21, 21, 21)
                                    .addComponent(txtPersonFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanelPersonLayout.createSequentialGroup()
                        .addGap(162, 162, 162)
                        .addComponent(btnRegisterAuthor)
                        .addGap(56, 56, 56)
                        .addComponent(btnRegisterManager)
                        .addGap(54, 54, 54)
                        .addComponent(btnRegisterNarrator)))
                .addContainerGap(138, Short.MAX_VALUE))
        );
        jPanelPersonLayout.setVerticalGroup(
            jPanelPersonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPersonLayout.createSequentialGroup()
                .addGap(153, 153, 153)
                .addGroup(jPanelPersonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelPersonID)
                    .addComponent(txtPersonID, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(jPanelPersonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelPersonName)
                    .addComponent(txtPersonFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelPersonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelPersonLastName)
                    .addComponent(txtPersonLastName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(jPanelPersonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegisterAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRegisterNarrator, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRegisterManager, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(219, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Persona", jPanelPerson);

        jLabelPublisherNIT.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabelPublisherNIT.setText("NIT");

        txtPublisherNIT.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtPublisherNIT.setToolTipText("");
        txtPublisherNIT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPublisherNITActionPerformed(evt);
            }
        });

        txtPublisherName.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtPublisherName.setToolTipText("");
        txtPublisherName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPublisherNameActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel7.setText("Nombre");

        jLabel8.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel8.setText("Dirección");

        txtPublisherAddress.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtPublisherAddress.setToolTipText("");
        txtPublisherAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPublisherAddressActionPerformed(evt);
            }
        });

        btnRegisterPublisher.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnRegisterPublisher.setText("Crear");
        btnRegisterPublisher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterPublisherActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel9.setText("Gerente");

        cmbManagerReg.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        cmbManagerReg.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione uno..." }));

        javax.swing.GroupLayout jPanelPublisherLayout = new javax.swing.GroupLayout(jPanelPublisher);
        jPanelPublisher.setLayout(jPanelPublisherLayout);
        jPanelPublisherLayout.setHorizontalGroup(
            jPanelPublisherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPublisherLayout.createSequentialGroup()
                .addGroup(jPanelPublisherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPublisherLayout.createSequentialGroup()
                        .addGap(273, 273, 273)
                        .addGroup(jPanelPublisherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabelPublisherNIT)
                            .addComponent(jLabel7)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelPublisherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtPublisherNIT)
                            .addComponent(txtPublisherName)
                            .addComponent(txtPublisherAddress)
                            .addComponent(cmbManagerReg, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelPublisherLayout.createSequentialGroup()
                        .addGap(361, 361, 361)
                        .addComponent(btnRegisterPublisher, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(285, Short.MAX_VALUE))
        );
        jPanelPublisherLayout.setVerticalGroup(
            jPanelPublisherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPublisherLayout.createSequentialGroup()
                .addGap(144, 144, 144)
                .addGroup(jPanelPublisherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPublisherLayout.createSequentialGroup()
                        .addComponent(jLabelPublisherNIT)
                        .addGap(15, 15, 15)
                        .addComponent(jLabel7)
                        .addGap(20, 20, 20)
                        .addGroup(jPanelPublisherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(txtPublisherAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelPublisherLayout.createSequentialGroup()
                        .addComponent(txtPublisherNIT, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13)
                        .addComponent(txtPublisherName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelPublisherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbManagerReg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(46, 46, 46)
                .addComponent(btnRegisterPublisher, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(175, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Editorial", jPanelPublisher);

        jLabel10.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel10.setText("Titulo");

        txtBookTitle.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtBookTitle.setToolTipText("");
        txtBookTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBookTitleActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel11.setText("Autores");

        jLabel12.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel12.setText("ISBN");

        txtBookISBN.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtBookISBN.setToolTipText("");
        txtBookISBN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBookISBNActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel13.setText("Genero");

        cmbBookGenre.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        cmbBookGenre.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione uno...", "Fantasía urbana", "Ciencia ficción distópica", "Realismo mágico", "Romance histórico", "Thriller psicológico", "Ficción filosófica", "Aventura steampunk", "Terror gótico", "No ficción narrativa", "Ficción postapocalíptica" }));

        btnRegisterBook.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnRegisterBook.setText("Crear");
        btnRegisterBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterBookActionPerformed(evt);
            }
        });

        cmbBookAuthorSelect.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        cmbBookAuthorSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione uno..." }));

        jLabel14.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel14.setText("Tipo");

        rbPrintedBook.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        rbPrintedBook.setText("Impreso");
        rbPrintedBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbPrintedBookActionPerformed(evt);
            }
        });

        rbDigitalBook.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        rbDigitalBook.setText("Digital");
        rbDigitalBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbDigitalBookActionPerformed(evt);
            }
        });

        rbAudiobook.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        rbAudiobook.setText("Audio Libro");
        rbAudiobook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbAudiobookActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel15.setText("Formato");

        cmbBookFormatSearch.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        cmbBookFormatSearch.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione uno..." }));
        cmbBookFormatSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbBookFormatSearchActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel16.setText("Valor");

        txtBookValue.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtBookValue.setToolTipText("");
        txtBookValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBookValueActionPerformed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel17.setText("Editorial");

        cmbPublisherBookReg.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        cmbPublisherBookReg.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione uno..." }));

        jLabel18.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel18.setText("Nro. Ejemplares");

        txtPrintedBookPages.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtPrintedBookPages.setToolTipText("");
        txtPrintedBookPages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPrintedBookPagesActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel19.setText("Nro. Paginas");

        txtPrintedBookCopies.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtPrintedBookCopies.setToolTipText("");
        txtPrintedBookCopies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPrintedBookCopiesActionPerformed(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel20.setText("Hipervinculo");

        txtDigitalBookHyperlink.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtDigitalBookHyperlink.setToolTipText("");
        txtDigitalBookHyperlink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDigitalBookHyperlinkActionPerformed(evt);
            }
        });

        txtAudiobookDuration.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtAudiobookDuration.setToolTipText("");
        txtAudiobookDuration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAudiobookDurationActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel21.setText("Duracion");

        jLabel22.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel22.setText("Narrador");

        cmbNarratorReg.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        cmbNarratorReg.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione uno..." }));

        btnAddAuthorToBook.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnAddAuthorToBook.setText("Agregar Autor");
        btnAddAuthorToBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAuthorToBookActionPerformed(evt);
            }
        });

        btnRemoveAuthorFromBook.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnRemoveAuthorFromBook.setText("Eliminar Autor");
        btnRemoveAuthorFromBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveAuthorFromBookActionPerformed(evt);
            }
        });

        txtAreaAuthorIDs.setColumns(20);
        txtAreaAuthorIDs.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtAreaAuthorIDs.setRows(5);
        txtAreaAuthorIDs.setEnabled(false);
        jScrollPane2.setViewportView(txtAreaAuthorIDs);

        javax.swing.GroupLayout jPanelBookLayout = new javax.swing.GroupLayout(jPanelBook);
        jPanelBook.setLayout(jPanelBookLayout);
        jPanelBookLayout.setHorizontalGroup(
            jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBookLayout.createSequentialGroup()
                .addGap(345, 345, 345)
                .addComponent(btnRegisterBook, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanelBookLayout.createSequentialGroup()
                .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelBookLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18)
                            .addComponent(jLabel19))
                        .addGap(20, 20, 20)
                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPrintedBookPages, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPrintedBookCopies, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(28, 28, 28)
                        .addComponent(jLabel20)
                        .addGap(16, 16, 16)
                        .addComponent(txtDigitalBookHyperlink, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel22)
                            .addComponent(jLabel21))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbNarratorReg, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtAudiobookDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanelBookLayout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelBookLayout.createSequentialGroup()
                                .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel11)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel16))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelBookLayout.createSequentialGroup()
                                        .addComponent(txtBookValue, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(jPanelBookLayout.createSequentialGroup()
                                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(cmbBookFormatSearch, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelBookLayout.createSequentialGroup()
                                                    .addComponent(rbPrintedBook)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(rbDigitalBook)))
                                            .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(txtBookTitle)
                                                .addComponent(txtBookISBN)
                                                .addComponent(cmbBookGenre, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(cmbBookAuthorSelect, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addGap(28, 28, 28)
                                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(rbAudiobook)
                                            .addComponent(btnRemoveAuthorFromBook, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(btnAddAuthorToBook, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(18, 18, 18)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))))
                            .addGroup(jPanelBookLayout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbPublisherBookReg, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanelBookLayout.setVerticalGroup(
            jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBookLayout.createSequentialGroup()
                .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelBookLayout.createSequentialGroup()
                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelBookLayout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(txtBookTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(10, 10, 10)
                                .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel11)
                                    .addComponent(cmbBookAuthorSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(txtBookISBN, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(17, 17, 17)
                                .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel13)
                                    .addComponent(cmbBookGenre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanelBookLayout.createSequentialGroup()
                                .addGap(37, 37, 37)
                                .addComponent(btnAddAuthorToBook, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnRemoveAuthorFromBook, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(rbPrintedBook)
                            .addComponent(rbDigitalBook)
                            .addComponent(rbAudiobook))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel15)
                            .addComponent(cmbBookFormatSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(txtBookValue, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(cmbPublisherBookReg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelBookLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(27, 27, 27)
                .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelBookLayout.createSequentialGroup()
                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18)
                            .addComponent(jLabel20)
                            .addComponent(txtDigitalBookHyperlink, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel19))
                    .addGroup(jPanelBookLayout.createSequentialGroup()
                        .addComponent(txtPrintedBookCopies, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtPrintedBookPages, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel21)))
                    .addGroup(jPanelBookLayout.createSequentialGroup()
                        .addGroup(jPanelBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel22)
                            .addComponent(cmbNarratorReg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(txtAudiobookDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(btnRegisterBook, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );

        jTabbedPane1.addTab("Libro", jPanelBook);

        cmbStandIDSelect.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        cmbStandIDSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione uno..." }));

        cmbPublisherStandAssign.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        cmbPublisherStandAssign.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione uno..." }));

        jLabel23.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel23.setText("Editoriales");

        jLabel24.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel24.setText("ID Stands");

        btnAddPublisherToStand.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnAddPublisherToStand.setText("Agregar Editorial");
        btnAddPublisherToStand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPublisherToStandActionPerformed(evt);
            }
        });

        btnConfirmStandAssignment.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnConfirmStandAssignment.setText("Comprar");
        btnConfirmStandAssignment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfirmStandAssignmentActionPerformed(evt);
            }
        });

        btnRemovePublisherFromStand.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnRemovePublisherFromStand.setText("Eliminar Editorial");
        btnRemovePublisherFromStand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemovePublisherFromStandActionPerformed(evt);
            }
        });

        txtAreaPublisherList.setColumns(20);
        txtAreaPublisherList.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtAreaPublisherList.setRows(5);
        txtAreaPublisherList.setEnabled(false);
        jScrollPane1.setViewportView(txtAreaPublisherList);

        btnAddStandToPurchase.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnAddStandToPurchase.setText("Agregar Stand");
        btnAddStandToPurchase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddStandToPurchaseActionPerformed(evt);
            }
        });

        btnRemoveStandFromPurchase.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnRemoveStandFromPurchase.setText("Eliminar Stand");
        btnRemoveStandFromPurchase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveStandFromPurchaseActionPerformed(evt);
            }
        });

        txtAreaStandIDs.setColumns(20);
        txtAreaStandIDs.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        txtAreaStandIDs.setRows(5);
        txtAreaStandIDs.setEnabled(false);
        jScrollPane3.setViewportView(txtAreaStandIDs);

        javax.swing.GroupLayout jPanelStandPurchaseLayout = new javax.swing.GroupLayout(jPanelStandPurchase);
        jPanelStandPurchase.setLayout(jPanelStandPurchaseLayout);
        jPanelStandPurchaseLayout.setHorizontalGroup(
            jPanelStandPurchaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStandPurchaseLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelStandPurchaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStandPurchaseLayout.createSequentialGroup()
                        .addComponent(btnConfirmStandAssignment)
                        .addGap(321, 321, 321))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStandPurchaseLayout.createSequentialGroup()
                        .addGroup(jPanelStandPurchaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelStandPurchaseLayout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbPublisherStandAssign, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnAddPublisherToStand)
                                .addGap(18, 18, 18)
                                .addComponent(btnRemovePublisherFromStand)))
                        .addGap(189, 189, 189))))
            .addGroup(jPanelStandPurchaseLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbStandIDSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelStandPurchaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelStandPurchaseLayout.createSequentialGroup()
                        .addComponent(btnAddStandToPurchase)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRemoveStandFromPurchase)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelStandPurchaseLayout.setVerticalGroup(
            jPanelStandPurchaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStandPurchaseLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanelStandPurchaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelStandPurchaseLayout.createSequentialGroup()
                        .addGroup(jPanelStandPurchaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel24)
                            .addComponent(cmbStandIDSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(226, 226, 226)
                        .addGroup(jPanelStandPurchaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(cmbPublisherStandAssign, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelStandPurchaseLayout.createSequentialGroup()
                        .addGroup(jPanelStandPurchaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddStandToPurchase, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRemoveStandFromPurchase, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3)
                        .addGap(8, 8, 8)
                        .addGroup(jPanelStandPurchaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddPublisherToStand, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRemovePublisherFromStand, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(btnConfirmStandAssignment, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(59, 59, 59))
        );

        jTabbedPane1.addTab("Comprar Stand", jPanelStandPurchase);

        tblPublishers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "NIT", "Nombre", "Dirección", "Nombre Gerente", "Nro. Stands"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(tblPublishers);

        btnLoadPublishersTable.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnLoadPublishersTable.setText("Consultar");
        btnLoadPublishersTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadPublishersTableActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelShowPublishersLayout = new javax.swing.GroupLayout(jPanelShowPublishers);
        jPanelShowPublishers.setLayout(jPanelShowPublishersLayout);
        jPanelShowPublishersLayout.setHorizontalGroup(
            jPanelShowPublishersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelShowPublishersLayout.createSequentialGroup()
                .addGroup(jPanelShowPublishersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelShowPublishersLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 759, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelShowPublishersLayout.createSequentialGroup()
                        .addGap(361, 361, 361)
                        .addComponent(btnLoadPublishersTable)))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        jPanelShowPublishersLayout.setVerticalGroup(
            jPanelShowPublishersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelShowPublishersLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(btnLoadPublishersTable, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39))
        );

        jTabbedPane1.addTab("Show Editoriales", jPanelShowPublishers);

        tblPeople.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Nombre Completo", "Tipo", "Editorial", "Nro. Libros"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(tblPeople);

        btnLoadPeopleTable.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnLoadPeopleTable.setText("Consultar");
        btnLoadPeopleTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadPeopleTableActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelShowPersonsLayout = new javax.swing.GroupLayout(jPanelShowPersons);
        jPanelShowPersons.setLayout(jPanelShowPersonsLayout);
        jPanelShowPersonsLayout.setHorizontalGroup(
            jPanelShowPersonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelShowPersonsLayout.createSequentialGroup()
                .addGroup(jPanelShowPersonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelShowPersonsLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 759, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelShowPersonsLayout.createSequentialGroup()
                        .addGap(361, 361, 361)
                        .addComponent(btnLoadPeopleTable)))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        jPanelShowPersonsLayout.setVerticalGroup(
            jPanelShowPersonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelShowPersonsLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(btnLoadPeopleTable, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39))
        );

        jTabbedPane1.addTab("Show Personas", jPanelShowPersons);

        tblStands.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "Precio", "Comprado", "Editoriales"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(tblStands);

        btnLoadStandsTable.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnLoadStandsTable.setText("Consultar");
        btnLoadStandsTable.setToolTipText("");
        btnLoadStandsTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadStandsTableActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelShowStandsLayout = new javax.swing.GroupLayout(jPanelShowStands);
        jPanelShowStands.setLayout(jPanelShowStandsLayout);
        jPanelShowStandsLayout.setHorizontalGroup(
            jPanelShowStandsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelShowStandsLayout.createSequentialGroup()
                .addGroup(jPanelShowStandsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelShowStandsLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 759, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelShowStandsLayout.createSequentialGroup()
                        .addGap(361, 361, 361)
                        .addComponent(btnLoadStandsTable)))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        jPanelShowStandsLayout.setVerticalGroup(
            jPanelShowStandsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelShowStandsLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(btnLoadStandsTable, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39))
        );

        jTabbedPane1.addTab("Show Stands", jPanelShowStands);

        tblBooks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Titulo", "Autores", "ISBN", "Genero", "Formato", "Valor", "Editorial", "Nro. Ejem", "Nro. Pag", "URL", "Narrador", "Duración"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane7.setViewportView(tblBooks);

        btnSearchBooksByType.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnSearchBooksByType.setText("Consultar");
        btnSearchBooksByType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchBooksByTypeActionPerformed(evt);
            }
        });

        cmbBookSearch.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        cmbBookSearch.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione uno...", "Libros Impresos", "Libros Digitales", "Audiolibros", "Todos los Libros" }));

        jLabel25.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel25.setText("Libros");

        javax.swing.GroupLayout jPanelShowBooksLayout = new javax.swing.GroupLayout(jPanelShowBooks);
        jPanelShowBooks.setLayout(jPanelShowBooksLayout);
        jPanelShowBooksLayout.setHorizontalGroup(
            jPanelShowBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelShowBooksLayout.createSequentialGroup()
                .addGroup(jPanelShowBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelShowBooksLayout.createSequentialGroup()
                        .addGap(361, 361, 361)
                        .addComponent(btnSearchBooksByType))
                    .addGroup(jPanelShowBooksLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 759, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelShowBooksLayout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(jLabel25)
                        .addGap(18, 18, 18)
                        .addComponent(cmbBookSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        jPanelShowBooksLayout.setVerticalGroup(
            jPanelShowBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelShowBooksLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanelShowBooksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbBookSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSearchBooksByType, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        jTabbedPane1.addTab("Show Libros", jPanelShowBooks);

        jLabel26.setFont(new java.awt.Font("Yu Gothic UI", 0, 24)); // NOI18N
        jLabel26.setText("Busqueda Libros");

        cmbAuthorSearch.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        cmbAuthorSearch.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione uno..." }));

        jLabel27.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel27.setText("Autor");

        btnSearchBooksByAuthor.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnSearchBooksByAuthor.setText("Consultar");
        btnSearchBooksByAuthor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchBooksByAuthorActionPerformed(evt);
            }
        });

        tblSearchResults.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Titulo", "Autores", "ISBN", "Genero", "Formato", "Valor", "Editorial", "Nro. Ejem", "Nro. Pag", "URL", "Narrador", "Duración"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane8.setViewportView(tblSearchResults);

        jLabel28.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        jLabel28.setText("Formato");

        cmbFormatSearch.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        cmbFormatSearch.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione uno...", "Pasta dura", "Pasta blanda", "EPUB", "PDF", "MOBI/AZW", "MP3", "MP4", "WAV", "WMA", "Otro" }));

        btnSearchBooksByFormat.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnSearchBooksByFormat.setText("Consultar");
        btnSearchBooksByFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchBooksByFormatActionPerformed(evt);
            }
        });

        jLabel29.setFont(new java.awt.Font("Yu Gothic UI", 0, 24)); // NOI18N
        jLabel29.setText("Autores con más Libros en Diferentes Editoriales");

        tblTopAuthors.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Nombre", "Cantidad"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane9.setViewportView(tblTopAuthors);

        btnSearchMaxPublishersAuthor.setFont(new java.awt.Font("Yu Gothic UI", 0, 18)); // NOI18N
        btnSearchMaxPublishersAuthor.setText("Consultar");
        btnSearchMaxPublishersAuthor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchMaxPublishersAuthorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelAditionalQueriesLayout = new javax.swing.GroupLayout(jPanelAditionalQueries);
        jPanelAditionalQueries.setLayout(jPanelAditionalQueriesLayout);
        jPanelAditionalQueriesLayout.setHorizontalGroup(
            jPanelAditionalQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAditionalQueriesLayout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addGroup(jPanelAditionalQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelAditionalQueriesLayout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanelAditionalQueriesLayout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbAuthorSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSearchBooksByAuthor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbFormatSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSearchBooksByFormat)
                        .addGap(40, 40, 40))))
            .addGroup(jPanelAditionalQueriesLayout.createSequentialGroup()
                .addGroup(jPanelAditionalQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelAditionalQueriesLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(jPanelAditionalQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 759, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelAditionalQueriesLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(jPanelAditionalQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 759, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel29)))))
                    .addGroup(jPanelAditionalQueriesLayout.createSequentialGroup()
                        .addGap(345, 345, 345)
                        .addComponent(btnSearchMaxPublishersAuthor)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanelAditionalQueriesLayout.setVerticalGroup(
            jPanelAditionalQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAditionalQueriesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelAditionalQueriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(cmbAuthorSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearchBooksByAuthor)
                    .addComponent(jLabel28)
                    .addComponent(cmbFormatSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearchBooksByFormat))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel29)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnSearchMaxPublishersAuthor)
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Consultas Adicionales", jPanelAditionalQueries);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtStandPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtStandPriceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtStandPriceActionPerformed

    private void txtStandIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtStandIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtStandIDActionPerformed

    private void txtPersonIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPersonIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPersonIDActionPerformed

    private void txtPersonFirstNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPersonFirstNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPersonFirstNameActionPerformed

    private void txtPersonLastNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPersonLastNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPersonLastNameActionPerformed

    private void txtPublisherNITActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPublisherNITActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPublisherNITActionPerformed

    private void txtPublisherNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPublisherNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPublisherNameActionPerformed

    private void txtPublisherAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPublisherAddressActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPublisherAddressActionPerformed

    private void txtBookTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBookTitleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBookTitleActionPerformed

    private void txtBookISBNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBookISBNActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBookISBNActionPerformed

    private void rbAudiobookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbAudiobookActionPerformed
        if (rbAudiobook.isSelected()) {
            rbDigitalBook.setSelected(false);
            rbPrintedBook.setSelected(false);
            txtPrintedBookPages.setEnabled(false);
            txtPrintedBookCopies.setEnabled(false);
            txtDigitalBookHyperlink.setEnabled(false);
            txtAudiobookDuration.setEnabled(true);
            cmbNarratorReg.setEnabled(true);
            
            cmbBookFormatSearch.removeAllItems();
            cmbBookFormatSearch.addItem("Seleccione uno...");
            cmbBookFormatSearch.addItem("MP3");
            cmbBookFormatSearch.addItem("MP4");
            cmbBookFormatSearch.addItem("WAV");
            cmbBookFormatSearch.addItem("WMA");
            cmbBookFormatSearch.addItem("Otro");
        }
    }//GEN-LAST:event_rbAudiobookActionPerformed

    private void txtBookValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBookValueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBookValueActionPerformed

    private void txtPrintedBookPagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPrintedBookPagesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPrintedBookPagesActionPerformed

    private void txtPrintedBookCopiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPrintedBookCopiesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPrintedBookCopiesActionPerformed

    private void txtDigitalBookHyperlinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDigitalBookHyperlinkActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDigitalBookHyperlinkActionPerformed

    private void txtAudiobookDurationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAudiobookDurationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAudiobookDurationActionPerformed

    private void rbPrintedBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbPrintedBookActionPerformed
        if (rbPrintedBook.isSelected()) {
            rbDigitalBook.setSelected(false);
            rbAudiobook.setSelected(false);
            txtPrintedBookPages.setEnabled(true);
            txtPrintedBookCopies.setEnabled(true);
            txtDigitalBookHyperlink.setEnabled(false);
            txtAudiobookDuration.setEnabled(false);
            cmbNarratorReg.setEnabled(false);
            
            cmbBookFormatSearch.removeAllItems();
            cmbBookFormatSearch.addItem("Seleccione uno...");
            cmbBookFormatSearch.addItem("Pasta dura");
            cmbBookFormatSearch.addItem("Pasta blanda");
        }
    }//GEN-LAST:event_rbPrintedBookActionPerformed

    private void rbDigitalBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbDigitalBookActionPerformed
        if (rbDigitalBook.isSelected()) {
            rbPrintedBook.setSelected(false);
            rbAudiobook.setSelected(false);
            txtPrintedBookPages.setEnabled(false);
            txtPrintedBookCopies.setEnabled(false);
            txtDigitalBookHyperlink.setEnabled(true);
            txtAudiobookDuration.setEnabled(false);
            cmbNarratorReg.setEnabled(false);
            
            cmbBookFormatSearch.removeAllItems();
            cmbBookFormatSearch.addItem("Seleccione uno...");
            cmbBookFormatSearch.addItem("EPUB");
            cmbBookFormatSearch.addItem("PDF");
            cmbBookFormatSearch.addItem("MOBI/AZW");
            cmbBookFormatSearch.addItem("Otro");
        }
    }//GEN-LAST:event_rbDigitalBookActionPerformed

    private void btnRegisterStandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterStandActionPerformed
        // TODO add your handling code here:
        String id = txtStandID.getText(); 
    String price = txtStandPrice.getText();
    
    // Llamar al Controlador
    ServiceResponse<Stand> response = controller.registerStand(id, price);
    
    if (response.isSuccess()) {
        JOptionPane.showMessageDialog(this, response.getMessage(), "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
        
        // Limpiar campos y actualizar ComboBox jComboBox7
        txtStandID.setText("");
        txtStandPrice.setText("");
        
        // Actualizamos el JComboBox con el ID del Stand
        Stand stand = response.getData();
        cmbStandIDSelect.addItem("" + stand.getId());
        
    } else {
        JOptionPane.showMessageDialog(this, response.getMessage(), "Error de Registro (" + response.getCode() + ")", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnRegisterStandActionPerformed

    private void btnRegisterAuthorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterAuthorActionPerformed
        // 1. CAPTURAR DATOS DE LA VISTA
    //Usar Long.toString(id) para el ID ya que los JTextFields devuelven String.
    String id = txtPersonID.getText(); // Usamos txtAuthorID o txtPersonID (el que hayas elegido)
    String firstName = txtPersonFirstName.getText();
    String lastName = txtPersonLastName.getText();
    
    // 2. LLAMAR AL CONTROLADOR (Invocación de la lógica de negocio)
    // El controlador maneja la validación de campos vacíos, de unicidad, y la persistencia.
    ServiceResponse<Author> response = controller.registerAuthor(id, firstName, lastName);
    
    // 3. MANEJAR LA RESPUESTA
    if (response.isSuccess()) {
        // Éxito (Código 200)
        
        JOptionPane.showMessageDialog(this, response.getMessage(), "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
        
        // Limpiar campos
        txtPersonID.setText("");
        txtPersonFirstName.setText("");
        txtPersonLastName.setText("");
        
    } else {
        // Error (Códigos 4xx o 5xx)
        
        // Mostrar mensaje de error del negocio (ej. "ID ya existe" o "Campos vacíos")
        JOptionPane.showMessageDialog(this, response.getMessage(), "Error de Registro (" + response.getCode() + ")", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnRegisterAuthorActionPerformed

    private void btnRegisterManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterManagerActionPerformed
        // TODO add your handling code here:
       // Asumimos que los inputs son txtPersonID, txtPersonFirstName, txtPersonLastName
    String id = txtPersonID.getText(); 
    String firstName = txtPersonFirstName.getText();
    String lastName = txtPersonLastName.getText();
    
    // 1. LLAMAR AL CONTROLADOR
    ServiceResponse<Manager> response = controller.registerManager(id, firstName, lastName);
    
    // 2. MANEJAR LA RESPUESTA
    if (response.isSuccess()) {
        
        JOptionPane.showMessageDialog(this, response.getMessage(), "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
        
        
        txtPersonID.setText("");
        txtPersonFirstName.setText("");
        txtPersonLastName.setText("");
        
        
        cmbManagerReg.addItem(id + " - " + firstName + " " + lastName);
        
    } else {
        // Error
        JOptionPane.showMessageDialog(this, response.getMessage(), "Error de Registro (" + response.getCode() + ")", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnRegisterManagerActionPerformed

    private void btnRegisterNarratorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterNarratorActionPerformed
        // TODO add your handling code here:
        String id = txtPersonID.getText(); 
    String firstName = txtPersonFirstName.getText();
    String lastName = txtPersonLastName.getText();
    
    // Llamar al Controlador
    ServiceResponse<Narrator> response = controller.registerNarrator(id, firstName, lastName);
    
    if (response.isSuccess()) {
        JOptionPane.showMessageDialog(this, response.getMessage(), "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
        
        // Limpiar campos y actualizar ComboBox jComboBox6
        txtPersonID.setText("");
        txtPersonFirstName.setText("");
        txtPersonLastName.setText("");
        
        // La Vista debe usar la entidad de la respuesta (response.getData()) para actualizar el JComboBox
        Narrator narrator = response.getData();
        cmbNarratorReg.addItem(narrator.getId() + " - " + narrator.getFullname());
        
    } else {
        JOptionPane.showMessageDialog(this, response.getMessage(), "Error de Registro (" + response.getCode() + ")", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnRegisterNarratorActionPerformed

    private void btnRegisterPublisherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterPublisherActionPerformed
        // TODO add your handling code here:
        String nit = txtPublisherNIT.getText();
    String name = txtPublisherName.getText();
    String address = txtPublisherAddress.getText();
    
    // Extracción del ID del Gerente seleccionado en el JComboBox
    String selectedManagerItem = (String) cmbManagerReg.getSelectedItem();
    String managerIdString = "";
    
    if (selectedManagerItem != null && selectedManagerItem.contains(" - ")) {
        // Asume que el formato es "ID - Nombre Completo"
        managerIdString = selectedManagerItem.split(" - ")[0]; 
    }
    
    // Llamar al Controlador
    ServiceResponse<Publisher> response = controller.registerPublisher(nit, name, address, managerIdString);
    
    if (response.isSuccess()) {
        JOptionPane.showMessageDialog(this, response.getMessage(), "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
        
        // Limpiar campos
        txtPublisherNIT.setText("");
        txtPublisherName.setText("");
        txtPublisherAddress.setText("");
        
        // Actualizar JComboBoxes que usan Editoriales
        Publisher publisher = response.getData();
        String publisherItem = publisher.getNit() + " - " + publisher.getName();
        cmbBookGenre.addItem(publisherItem); // Usado para libros
        cmbBookFormatSearch.addItem(publisherItem); // Usado para asignación de stands
        
    } else {
        JOptionPane.showMessageDialog(this, response.getMessage(), "Error de Registro (" + response.getCode() + ")", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnRegisterPublisherActionPerformed

    private void btnAddAuthorToBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAuthorToBookActionPerformed
        // TODO add your handling code here:
        String author = (String) cmbBookAuthorSelect.getSelectedItem();
    
        // Validación: No agregar si es el default o si está vacío
        if (author == null || author.equals("Seleccione uno...") || author.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un autor válido.");
            return;
        }

        // Validación: Evitar duplicados visuales en el TextArea
        if (txtAreaAuthorIDs.getText().contains(author)) {
            JOptionPane.showMessageDialog(this, "Este autor ya está en la lista.");
            return;
        }

        txtAreaAuthorIDs.append(author + "\n");
    }//GEN-LAST:event_btnAddAuthorToBookActionPerformed

    private void btnRemoveAuthorFromBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveAuthorFromBookActionPerformed
        // TODO add your handling code here:
        String author = cmbBookAuthorSelect.getItemAt(cmbBookAuthorSelect.getSelectedIndex());
        txtAreaAuthorIDs.setText(txtAreaAuthorIDs.getText().replace(author + "\n", ""));
    }//GEN-LAST:event_btnRemoveAuthorFromBookActionPerformed

    private void btnRegisterBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterBookActionPerformed
        // --- A. Captura y Limpieza de Datos Comunes ---
        String title = txtBookTitle.getText().trim();
        String authorIdsString = txtAreaAuthorIDs.getText(); 
        String isbn = txtBookISBN.getText().trim();

        // 1. CORRECCIÓN GÉNERO: Usar cmbBookGenre (no cmbBookSearch)
        String format = "";
        if (cmbBookFormatSearch.getSelectedItem() != null) {
            format = cmbBookFormatSearch.getSelectedItem().toString();
            if (format.equals("Seleccione uno...")) {
                format = ""; // Para que falle la validación si no escogen nada
            }
        }
        String genre = "";
        if (cmbBookGenre.getSelectedItem() != null) {
            genre = cmbBookGenre.getSelectedItem().toString().trim();
            // Validación extra: Evitar guardar "Seleccione uno..." como género
            if (genre.equals("Seleccione uno...")) {
                 genre = ""; // Esto disparará el error 400 correctamente si no se elige nada
            }
        }

        String valueString = txtBookValue.getText().trim();

        String publisherItem = "";
        if (cmbPublisherBookReg.getSelectedItem() != null) {
            publisherItem = cmbPublisherBookReg.getSelectedItem().toString();
        }

        String publisherNit = "";
        if (!publisherItem.isEmpty() && publisherItem.contains(" - ")) {
            publisherNit = publisherItem.split(" - ")[0].trim(); // Extrae el NIT
        }

        // Limpieza de IDs de Autores (Tu código ya estaba bien aquí)
        List<String> authorIDsList = java.util.Arrays.stream(authorIdsString.split("\\n"))
            .map(String::trim)
            .filter(id -> !id.isEmpty())
            .map(line -> {
            // Si la línea tiene el formato "123 - Nombre", tomamos solo "123"
            if (line.contains(" - ")) {
                return line.split(" - ")[0].trim();
            }
            // Si la línea es solo "123", la dejamos igual
            return line;
            })
            .collect(java.util.stream.Collectors.toList());
        
        // --- B. Determinar Tipo de Libro y Captura de Datos Específicos ---
        String bookType = "";
        String pagesString = "";
        String copiesString = "";
        String hyperlink = "";
        String durationString = "";
        String narratorIdString = "";

        if (rbPrintedBook.isSelected()) {
            bookType = "IMPRESO";
            pagesString = txtPrintedBookPages.getText().trim();
            copiesString = txtPrintedBookCopies.getText().trim();

        } else if (rbDigitalBook.isSelected()) {
            bookType = "DIGITAL";
            hyperlink = txtDigitalBookHyperlink.getText().trim();

        } else if (rbAudiobook.isSelected()) {
            bookType = "AUDIO";
            durationString = txtAudiobookDuration.getText().trim();

            String selectedNarratorItem = (String) cmbNarratorReg.getSelectedItem();
            if (selectedNarratorItem != null && selectedNarratorItem.contains(" - ")) {
                narratorIdString = selectedNarratorItem.split(" - ")[0].trim(); // Extrae y limpia el ID
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un tipo de libro.", "Error de Selección", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- C. Llamar al Controlador ---
        // NOTA: Se ha reemplazado authorIdsString por authorIDsList (List<String>)
        ServiceResponse<Book> response = controller.registerBook(
            title, 
            authorIDsList, 
            isbn, 
            genre, 
            format,       
            valueString, 
            publisherNit, 
            bookType, 
            pagesString, 
            copiesString, 
            hyperlink, 
            durationString, 
            narratorIdString
        );

        // --- D. Manejar Respuesta ---
            if (response.isSuccess()) {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);

            // 1. Limpiar campos de texto simples
            txtBookTitle.setText("");
            txtBookISBN.setText("");
            txtBookValue.setText("");

            // 2. Limpiar campos específicos de tipo
            txtPrintedBookPages.setText("");
            txtPrintedBookCopies.setText("");
            txtDigitalBookHyperlink.setText("");
            txtAudiobookDuration.setText("");

            // 3.Limpiar la lista de autores acumulados
            txtAreaAuthorIDs.setText(""); 
            // Nota: Las tablas y combos se actualizan solos gracias al this.update() del Observer

        } else {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error de Registro (" + response.getCode() + ")", JOptionPane.ERROR_MESSAGE);
        }
    
    }//GEN-LAST:event_btnRegisterBookActionPerformed

    private void btnAddStandToPurchaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddStandToPurchaseActionPerformed
        // TODO add your handling code here:
        String stand = cmbStandIDSelect.getItemAt(cmbStandIDSelect.getSelectedIndex());
        txtAreaStandIDs.append(stand + "\n");
    }//GEN-LAST:event_btnAddStandToPurchaseActionPerformed

    private void btnRemoveStandFromPurchaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveStandFromPurchaseActionPerformed
        // TODO add your handling code here:
        String stand = cmbStandIDSelect.getItemAt(cmbStandIDSelect.getSelectedIndex());
        txtAreaStandIDs.setText(txtAreaStandIDs.getText().replace(stand + "\n", ""));
    }//GEN-LAST:event_btnRemoveStandFromPurchaseActionPerformed

    private void btnAddPublisherToStandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPublisherToStandActionPerformed
        // TODO add your handling code here:
        String publisher = cmbPublisherStandAssign.getItemAt(cmbPublisherStandAssign.getSelectedIndex());
        txtAreaPublisherList.append(publisher + "\n");
    }//GEN-LAST:event_btnAddPublisherToStandActionPerformed

    private void btnRemovePublisherFromStandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemovePublisherFromStandActionPerformed
        // TODO add your handling code here:
        String publisher = cmbPublisherStandAssign.getItemAt(cmbPublisherStandAssign.getSelectedIndex());
        txtAreaPublisherList.setText(txtAreaPublisherList.getText().replace(publisher + "\n", ""));
    }//GEN-LAST:event_btnRemovePublisherFromStandActionPerformed

    private void btnConfirmStandAssignmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmStandAssignmentActionPerformed
    // 1. CORRECCIÓN: Usar el ComboBox correcto (cmbPublisherStandAssign)
    String publisherItem = (String) cmbPublisherStandAssign.getSelectedItem();
    String publisherNit = "";

    // Validar que se haya seleccionado algo válido
    if (publisherItem != null && publisherItem.contains(" - ")) {
        publisherNit = publisherItem.split(" - ")[0].trim(); // Extrae el NIT
    } else {
        JOptionPane.showMessageDialog(this, "Debe seleccionar una Editorial válida.", "Error de Selección", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 2. Captura y Limpieza de IDs de Stand (Esto ya estaba bien, pero aseguramos robustez)
    String standIdsText = txtAreaStandIDs.getText().trim();
    if (standIdsText.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Debe agregar al menos un ID de Stand al carrito.", "Error de Asignación", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Convertir IDs a List<Long>
    List<Long> standIds = java.util.Arrays.stream(standIdsText.split("\\n"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(s -> {
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException e) {
                    return -1L; // Marcador para error
                }
            })
            .filter(id -> id != -1L) 
            .collect(java.util.stream.Collectors.toList());

    if (standIds.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No se detectaron IDs de Stand válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 3. Llamada al Controlador
    ServiceResponse<Publisher> response = controller.assignStandsToPublisher(publisherNit, standIds);

    // 4. Manejar Respuesta
    if (response.isSuccess()) {
        JOptionPane.showMessageDialog(this, response.getMessage(), "Asignación Exitosa", JOptionPane.INFORMATION_MESSAGE);
        
        // Limpiar carrito y actualizar
        txtAreaStandIDs.setText("");
        // La actualización de tablas es automática por el Observer, pero limpiamos la visualización de la lista de editoriales también si quieres
        txtAreaPublisherList.setText(""); 
        
    } else {
        JOptionPane.showMessageDialog(this, response.getMessage(), "Error de Asignación (" + response.getCode() + ")", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnConfirmStandAssignmentActionPerformed

    private void btnLoadPublishersTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadPublishersTableActionPerformed
        // TODO add your handling code here:
        this.update();
    }//GEN-LAST:event_btnLoadPublishersTableActionPerformed

    private void btnLoadPeopleTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadPeopleTableActionPerformed
        // TODO add your handling code here:
        this.update();
    }//GEN-LAST:event_btnLoadPeopleTableActionPerformed

    private void btnLoadStandsTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadStandsTableActionPerformed
        // TODO add your handling code here:
        this.update();
    }//GEN-LAST:event_btnLoadStandsTableActionPerformed

    private void btnSearchBooksByTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchBooksByTypeActionPerformed
        // TODO add your handling code here:
        String search = cmbBookSearch.getItemAt(cmbBookSearch.getSelectedIndex());
    
        DefaultTableModel model = (DefaultTableModel) tblBooks.getModel();
        model.setRowCount(0);

        // ERROR CORREGIDO: Usamos controller.getBooks()
        for (Book book : controller.getBooks()) { // <-- CORREGIDO
            if (search.equals("Libros Impresos") && book instanceof PrintedBook printedBook) {
                // ... lógica de PrintedBook
                 String authors = printedBook.getAuthors().get(0).getFullname();
                 for (int i = 1; i < printedBook.getAuthors().size(); i++) {
                     authors += (", " + printedBook.getAuthors().get(i).getFullname());
                 }
                 model.addRow(new Object[]{printedBook.getTitle(), authors, printedBook.getIsbn(), printedBook.getGenre(), printedBook.getFormat(), printedBook.getValue(), printedBook.getPublisher().getName(), printedBook.getCopies(), printedBook.getPages(), "-", "-", "-"});
            }
            if (search.equals("Libros Digitales") && book instanceof DigitalBook digitalBook) {
                // ... lógica de DigitalBook
                 String authors = digitalBook.getAuthors().get(0).getFullname();
                 for (int i = 1; i < digitalBook.getAuthors().size(); i++) {
                     authors += (", " + digitalBook.getAuthors().get(i).getFullname());
                 }
                 model.addRow(new Object[]{digitalBook.getTitle(), authors, digitalBook.getIsbn(), digitalBook.getGenre(), digitalBook.getFormat(), digitalBook.getValue(), digitalBook.getPublisher().getName(), "-", "-", digitalBook.hasHyperlink() ? digitalBook.getHyperlink() : "No", "-", "-"});
            }
            if (search.equals("Audiolibros") && book instanceof Audiobook audiobook) {
                // ... lógica de Audiobook
                 String authors = audiobook.getAuthors().get(0).getFullname();
                 for (int i = 1; i < audiobook.getAuthors().size(); i++) {
                     authors += (", " + audiobook.getAuthors().get(i).getFullname());
                 }
                 model.addRow(new Object[]{audiobook.getTitle(), authors, audiobook.getIsbn(), audiobook.getGenre(), audiobook.getFormat(), audiobook.getValue(), audiobook.getPublisher().getName(), "-", "-", "-", audiobook.getNarrator().getFullname(), audiobook.getDuration()});
            }
            if (search.equals("Todos los Libros")) {
                // ... lógica de Todos los Libros
                 String authors = book.getAuthors().get(0).getFullname();
                 for (int i = 1; i < book.getAuthors().size(); i++) {
                     authors += (", " + book.getAuthors().get(i).getFullname());
                 }
                 if (book instanceof PrintedBook printedBook) {
                     model.addRow(new Object[]{printedBook.getTitle(), authors, printedBook.getIsbn(), printedBook.getGenre(), printedBook.getFormat(), printedBook.getValue(), printedBook.getPublisher().getName(), printedBook.getCopies(), printedBook.getPages(), "-", "-", "-"});
                 }
                 if (book instanceof DigitalBook digitalBook) {
                     model.addRow(new Object[]{digitalBook.getTitle(), authors, digitalBook.getIsbn(), digitalBook.getGenre(), digitalBook.getFormat(), digitalBook.getValue(), digitalBook.getPublisher().getName(), "-", "-", digitalBook.hasHyperlink() ? digitalBook.getHyperlink() : "No", "-", "-"});
                 }
                 if (book instanceof Audiobook audiobook) {
                     model.addRow(new Object[]{audiobook.getTitle(), authors, audiobook.getIsbn(), audiobook.getGenre(), audiobook.getFormat(), audiobook.getValue(), audiobook.getPublisher().getName(), "-", "-", "-", audiobook.getNarrator().getFullname(), audiobook.getDuration()});
                 }
            }
        }
    }//GEN-LAST:event_btnSearchBooksByTypeActionPerformed

    private void btnSearchBooksByAuthorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchBooksByAuthorActionPerformed
        // TODO add your handling code here:
        performSearch();
    }//GEN-LAST:event_btnSearchBooksByAuthorActionPerformed

    private void btnSearchBooksByFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchBooksByFormatActionPerformed
        // TODO add your handling code here:
        performSearch();
    }//GEN-LAST:event_btnSearchBooksByFormatActionPerformed

    private void btnSearchMaxPublishersAuthorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchMaxPublishersAuthorActionPerformed
        // TODO add your handling code here:
        ServiceResponse<Map<Author, Long>> response = controller.searchAuthorsByPublisherDiversity();
    
        if (response.isSuccess()) {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Consulta Exitosa", JOptionPane.INFORMATION_MESSAGE);
            this.update(); 
        } else {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Error en Consulta (" + response.getCode() + ")", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSearchMaxPublishersAuthorActionPerformed

    private void cmbBookFormatSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbBookFormatSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbBookFormatSearchActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        System.setProperty("flatlaf.useNativeLibrary", "false");
    
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        /* ********** PASOS MVC CRUCIALES ********** */
        // PASO 1: Instanciar el Controlador (La clase que maneja la lógica de negocio).
        MegaferiaController controller = new MegaferiaController(); // <--- AÑADIR ESTA LÍNEA
        /* **************************************** */

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // PASO 2: Crear la Vista y PASARLE el Controlador (Inyección de Dependencias).
                new MegaferiaFrame(controller).setVisible(true); // <--- CORRECCIÓN CLAVE
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddAuthorToBook;
    private javax.swing.JButton btnAddPublisherToStand;
    private javax.swing.JButton btnAddStandToPurchase;
    private javax.swing.JButton btnConfirmStandAssignment;
    private javax.swing.JButton btnLoadPeopleTable;
    private javax.swing.JButton btnLoadPublishersTable;
    private javax.swing.JButton btnLoadStandsTable;
    private javax.swing.JButton btnRegisterAuthor;
    private javax.swing.JButton btnRegisterBook;
    private javax.swing.JButton btnRegisterManager;
    private javax.swing.JButton btnRegisterNarrator;
    private javax.swing.JButton btnRegisterPublisher;
    private javax.swing.JButton btnRegisterStand;
    private javax.swing.JButton btnRemoveAuthorFromBook;
    private javax.swing.JButton btnRemovePublisherFromStand;
    private javax.swing.JButton btnRemoveStandFromPurchase;
    private javax.swing.JButton btnSearchBooksByAuthor;
    private javax.swing.JButton btnSearchBooksByFormat;
    private javax.swing.JButton btnSearchBooksByType;
    private javax.swing.JButton btnSearchMaxPublishersAuthor;
    private javax.swing.JComboBox<String> cmbAuthorSearch;
    private javax.swing.JComboBox<String> cmbBookAuthorSelect;
    private javax.swing.JComboBox<String> cmbBookFormatSearch;
    private javax.swing.JComboBox<String> cmbBookGenre;
    private javax.swing.JComboBox<String> cmbBookSearch;
    private javax.swing.JComboBox<String> cmbFormatSearch;
    private javax.swing.JComboBox<String> cmbManagerReg;
    private javax.swing.JComboBox<String> cmbNarratorReg;
    private javax.swing.JComboBox<String> cmbPublisherBookReg;
    private javax.swing.JComboBox<String> cmbPublisherStandAssign;
    private javax.swing.JComboBox<String> cmbStandIDSelect;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelPersonID;
    private javax.swing.JLabel jLabelPersonLastName;
    private javax.swing.JLabel jLabelPersonName;
    private javax.swing.JLabel jLabelPublisherNIT;
    private javax.swing.JLabel jLabelStandID;
    private javax.swing.JLabel jLabelStandPrice;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelAditionalQueries;
    private javax.swing.JPanel jPanelBook;
    private javax.swing.JPanel jPanelPerson;
    private javax.swing.JPanel jPanelPublisher;
    private javax.swing.JPanel jPanelShowBooks;
    private javax.swing.JPanel jPanelShowPersons;
    private javax.swing.JPanel jPanelShowPublishers;
    private javax.swing.JPanel jPanelShowStands;
    private javax.swing.JPanel jPanelStand;
    private javax.swing.JPanel jPanelStandPurchase;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JRadioButton rbAudiobook;
    private javax.swing.JRadioButton rbDigitalBook;
    private javax.swing.JRadioButton rbPrintedBook;
    private javax.swing.JTable tblBooks;
    private javax.swing.JTable tblPeople;
    private javax.swing.JTable tblPublishers;
    private javax.swing.JTable tblSearchResults;
    private javax.swing.JTable tblStands;
    private javax.swing.JTable tblTopAuthors;
    private javax.swing.JTextArea txtAreaAuthorIDs;
    private javax.swing.JTextArea txtAreaPublisherList;
    private javax.swing.JTextArea txtAreaStandIDs;
    private javax.swing.JTextField txtAudiobookDuration;
    private javax.swing.JTextField txtBookISBN;
    private javax.swing.JTextField txtBookTitle;
    private javax.swing.JTextField txtBookValue;
    private javax.swing.JTextField txtDigitalBookHyperlink;
    private javax.swing.JTextField txtPersonFirstName;
    private javax.swing.JTextField txtPersonID;
    private javax.swing.JTextField txtPersonLastName;
    private javax.swing.JTextField txtPrintedBookCopies;
    private javax.swing.JTextField txtPrintedBookPages;
    private javax.swing.JTextField txtPublisherAddress;
    private javax.swing.JTextField txtPublisherNIT;
    private javax.swing.JTextField txtPublisherName;
    private javax.swing.JTextField txtStandID;
    private javax.swing.JTextField txtStandPrice;
    // End of variables declaration//GEN-END:variables


}
