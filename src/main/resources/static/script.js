$(document).ready(function () {
    const socket = new SockJS('/websocket');
    const stompClient = Stomp.over(socket);
    console.log("started");
    // Extract pagination and sorting parameters from the URL
    const urlParams = new URLSearchParams(window.location.search);
    const currentPage = parseInt(urlParams.get('page') || 0);
    const pageSize = parseInt(urlParams.get('size') || 10);
    const sortBy = urlParams.get('sortBy') || 'name';
    const sortDirection = urlParams.get('sortDirection') || 'asc';

    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/chapters', function (message) {
            console.log("got");
            console.log(message);
            const data = JSON.parse(message.body);
            handleChapterEvent(data);
        });
    });

    function handleChapterEvent(data) {
        console.log("got");
        console.log(data);
        if (data.id) {
            updateOrCreateChapterRow(data);
        } else {
            console.log("deleting");
            removeChapterRow(data.deleteID);
        }
    }

    function updateOrCreateChapterRow(chapter) {
        let $row = $(`#${chapter.id}`);
        if ($row.length) {
            $row.remove();
        }
        update(chapter);
    }

    function removeChapterRow(chapterId) {
        $(`#${chapterId}`).remove();
        const rows = $('tbody tr').get();
        console.log(rows);
        if (rows.length < pageSize) {
            console.log("update");
            location.reload(true);
        }
    }

    function update(chapter) {
        // Collect all rows and sort by sortBy field
        const rows = $('tbody tr').get();

        const newRow = `
            <tr id="${chapter.id}">
                <td>${chapter.name}</td>
                <td>${chapter.world}</td>
                <td>${chapter.marinesCount || ''}</td>
                <td>${chapter.owner.username}</td>
                <td>
                    <a href="/chapters/edit/${chapter.id}">Редактировать</a>
                    <form action="/chapters/delete/${chapter.id}" method="post" style="display:inline;">
                        <button type="submit">Удалить</button>
                    </form>
                    <a href="/chapters/${chapter.id}">Космодесантники</a>
                </td>
            </tr>
        `;

        console.log(rows);
        console.log($(newRow)[0])

        rows.push($(newRow)[0]);
        console.log(rows);

        rows.sort(function (a, b) {
            let valA = $(a).find(`td:nth-child(${getColumnIndex(sortBy)})`).text();
            let valB = $(b).find(`td:nth-child(${getColumnIndex(sortBy)})`).text();

            if (getColumnIndex(sortBy) == 3) {
                valA = parseInt(valA);
                valB = parseInt(valB);
            }

            console.log(valA, valB);

            if (valA < valB) return sortDirection === 'asc' ? -1 : 1;
            if (valA > valB) return sortDirection === 'asc' ? 1 : -1;
            return 0;
        });

        console.log(rows);

        if (rows.length > pageSize) {
            rows.pop();
        }

        $('tbody').empty();

        rows.forEach(row => {
            $('tbody').append(row);
        });

    }

    function getColumnIndex(field) {
        // Map sortBy fields to table column indices (1-based)
        const columnMap = {
            name: 1,
            world: 2,
            marinesCount: 3,
            owner: 4 // Assuming sorting by owner.username
        };
        return columnMap[field] || 1; // Default to name column
    }
});