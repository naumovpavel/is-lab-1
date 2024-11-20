$(document).ready(function () {
    const socket = new SockJS('/websocket');
    const stompClient = Stomp.over(socket);
    const urlParams = new URLSearchParams(window.location.search);
    const currentPage = parseInt($('#last-page').text());
    let offset = 0;
    const pageSize = parseInt(urlParams.get('size') || 10);
    const sortBy = urlParams.get('sortBy') || 'x';
    const sortDirection = urlParams.get('sortDirection') || 'asc';

    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/coordinates', function (message) {
            const data = JSON.parse(message.body);
            handleCoordinateEvent(data);
        });
    });

    function handleCoordinateEvent(data) {
        if (data.id) {
            updateOrCreateCoordinateRow(data);
        } else {
            offset--;
            removeCoordinateRow(data.deleteID);
        }
        $('#last-page').text(currentPage + Math.floor(offset / pageSize));
    }

    function updateOrCreateCoordinateRow(coordinate) {
        let $row = $(`#${coordinate.id}`);
        if ($row.length) {
            $row.remove();
        } else {
            offset++;
        }
        update(coordinate);
    }

    function removeCoordinateRow(coordinateId) {
        $(`#${coordinateId}`).remove();
        const rows = $('tbody tr').get();
        if (rows.length < pageSize) {
            location.reload(true);
        }
    }

    function update(coordinate) {
        const rows = $('tbody tr').get();

        const newRow = `
            <tr id="${coordinate.id}">
                <td>${coordinate.x}</td>
                <td>${coordinate.y}</td>
                <td>
                    <a href="/coordinates/edit/${coordinate.id}">Edit</a>
                    <form action="/coordinates/delete/${coordinate.id}" method="post" style="display:inline;">
                        <button type="submit">Delete</button>
                    </form>
                </td>
            </tr>
        `;

        rows.push($(newRow)[0]);

        rows.sort(function (a, b) {
            let valA = $(a).find(`td:nth-child(${getColumnIndex(sortBy)})`).text();
            let valB = $(b).find(`td:nth-child(${getColumnIndex(sortBy)})`).text();

            if (getColumnIndex(sortBy) == 1 || getColumnIndex(sortBy) == 2) {
                valA = parseFloat(valA) || 0;
                valB = parseFloat(valB) || 0;
            }

            if (valA < valB) return sortDirection === 'asc' ? -1 : 1;
            if (valA > valB) return sortDirection === 'asc' ? 1 : -1;
            return 0;
        });

        if (rows.length > pageSize) {
            rows.pop();
        }

        $('tbody').empty();

        rows.forEach(row => {
            $('tbody').append(row);
        });

    }

    function getColumnIndex(field) {
        const columnMap = {
            x: 1,
            y: 2
        };
        return columnMap[field] || 1;
    }
});