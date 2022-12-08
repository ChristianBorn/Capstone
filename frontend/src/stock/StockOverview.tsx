import React, {useEffect, useState} from 'react';
import axios from "axios";
import {StockItemModel} from "./StockItemModel";
import AddIcon from "../icons/AddIcon";
import {BounceLoader} from "react-spinners";
import DeleteIcon from "../icons/DeleteIcon";
import AddItemModal from "./AddStockItemModal";
import DeleteItemModal from "./DeleteItemModal";
import EditIcon from "../icons/EditIcon";
import EditItemModal from "./EditItemModal";

function StockOverview() {

    const [stockItems, setStockItems] = useState<StockItemModel[]>()
    const [successMessage, setSuccessMessage] = useState<string>()
    const [idToDelete, setIdToDelete] = useState<string>("")
    const [dailyConsumption] = useState(13)
    const [itemToEdit, setItemToEdit] = useState<StockItemModel>({
        id: "", name: "", amountInStock: 0, pricePerKilo: 0, type: ""
    })
    const [openModal, setOpenModal] = useState<"add" | "edit" | "delete" | "addConsumption">()


    const getAllStockItems = () => {
        axios.get("/stock/")
            .then((response) => response.data)
            .catch((error) => console.error("Error while getting Stockitems:" + error))
            .then(setStockItems)
    }
    const openAddModal = () => {
        setOpenModal("add")
        setSuccessMessage("")
    }
    const openDeleteModal = (id: string) => {
        setOpenModal("delete")
        setIdToDelete(id)
        setSuccessMessage("")
    }
    const openEditModal = (itemToEdit: StockItemModel) => {
        setOpenModal("edit")
        setItemToEdit(itemToEdit)
        setSuccessMessage("")
    }

    const closeModal = () => {
        setOpenModal(undefined)
    }


    useEffect(() => {
        getAllStockItems()
    }, [])


    if (stockItems === undefined) {
        return <BounceLoader
            size={100}
            aria-label="Loading Spinner"
            data-testid="loader"
            color="#36d7b7"
            cssOverride={{
                margin: "0 auto"
            }}
        />
    }



    return (
        <>

            <AddItemModal modalIsOpen={openModal === "add"}
                          closeModal={closeModal}
                          reloadStockItems={getAllStockItems}
                          setSuccessMessage={setSuccessMessage}/>
            <DeleteItemModal modalIsOpen={openModal === "delete"}
                             closeModal={closeModal}
                             reloadStockItems={getAllStockItems}
                             setSuccessMessage={setSuccessMessage} idToDelete={idToDelete}/>
            <EditItemModal modalIsOpen={openModal === "edit"}
                           closeModal={closeModal}
                           reloadStockItems={getAllStockItems}
                           setSuccessMessage={setSuccessMessage} itemToEdit={itemToEdit}/>
            {stockItems.length > 0 ?
                <>
                    <div className={"overview-table-wrapper"}>
                        <table>

                            <thead>
                            <tr>
                                <th>Name</th>
                                <th>Typ</th>
                                <th>Verfügbare Menge in <abbr title={"Kilogramm"}>kg</abbr></th>
                                <th>Preis pro <abbr title={"Kilogramm"}>kg</abbr></th>
                                <th>Täglicher Verbrauch</th>
                                <th>Aufgebraucht in</th>
                                <th>Aktionen</th>
                            </tr>
                            </thead>
                            <tbody>
                            {stockItems.map(item => {
                                return <tr key={item.id}>
                                    <td><strong>{item.name}</strong></td>
                                    <td>{item.type}</td>
                                    <td>{item.amountInStock}</td>
                                    <td>{item.pricePerKilo}</td>
                                    <td>{dailyConsumption}</td>
                                    <td>{Math.round(item.amountInStock / dailyConsumption)} Tagen</td>
                                    <td>
                                        <div className={"action-cell"}>
                                            <EditIcon onClickAction={openEditModal}
                                                      itemToEdit={item}/>
                                            <DeleteIcon onClickAction={openDeleteModal}
                                                        idToDelete={item.id}/>
                                        </div>
                                    </td>
                                </tr>
                            })
                            }
                            </tbody>
                        </table>

                    </div>
                    {successMessage && <div className={"success-message"}>{successMessage}</div>}
                </>
                :
                <div>
                    {successMessage && <div className={"success-message"}>{successMessage}</div>}
                    <p>Keine Items im Lager</p>
                </div>
            }
            <AddIcon openModal={openAddModal} title={"Neuen Eintrag hinzufügen"}/>
        </>
    );
}

export default StockOverview;
